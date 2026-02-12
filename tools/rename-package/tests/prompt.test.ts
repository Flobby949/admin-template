import { describe, it, expect, vi } from 'vitest';
import type { Interface as ReadlineInterface } from 'node:readline';
import { promptPackageName, promptYesNo, promptGroupId, runInteractiveMode } from '../src/prompt.js';
import type { CreateRL } from '../src/prompt.js';

/**
 * Create a mock CreateRL factory that feeds answers sequentially.
 */
function createMockRL(answers: string[]): CreateRL {
  let callIndex = 0;
  return () => {
    const rl = {
      question: (_prompt: string, cb: (answer: string) => void) => {
        const answer = answers[callIndex] ?? '';
        callIndex++;
        cb(answer);
      },
      close: vi.fn(),
    } as unknown as ReadlineInterface;
    return rl;
  };
}

describe('promptPackageName', () => {
  it('should return a valid package name on first try', async () => {
    const createRL = createMockRL(['com.example.demo']);
    const result = await promptPackageName('Old package name', createRL);
    expect(result).toBe('com.example.demo');
  });

  it('should retry on invalid input and return the first valid one', async () => {
    const createRL = createMockRL(['INVALID', 'com..bad', 'org.valid.pkg']);
    const result = await promptPackageName('New package name', createRL);
    expect(result).toBe('org.valid.pkg');
  });

  it('should retry on single-segment input', async () => {
    const createRL = createMockRL(['example', 'com.example']);
    const result = await promptPackageName('Package', createRL);
    expect(result).toBe('com.example');
  });

  it('should trim whitespace from input', async () => {
    const createRL = createMockRL(['  com.example.app  ']);
    const result = await promptPackageName('Package', createRL);
    expect(result).toBe('com.example.app');
  });

  it('should retry on empty input', async () => {
    const createRL = createMockRL(['', '  ', 'com.valid.pkg']);
    const result = await promptPackageName('Package', createRL);
    expect(result).toBe('com.valid.pkg');
  });
});

describe('promptYesNo', () => {
  it('should return true for "Y"', async () => {
    const createRL = createMockRL(['Y']);
    const result = await promptYesNo('Continue?', true, createRL);
    expect(result).toBe(true);
  });

  it('should return true for "y"', async () => {
    const createRL = createMockRL(['y']);
    const result = await promptYesNo('Continue?', true, createRL);
    expect(result).toBe(true);
  });

  it('should return true for "yes"', async () => {
    const createRL = createMockRL(['yes']);
    const result = await promptYesNo('Continue?', true, createRL);
    expect(result).toBe(true);
  });

  it('should return false for "n"', async () => {
    const createRL = createMockRL(['n']);
    const result = await promptYesNo('Continue?', true, createRL);
    expect(result).toBe(false);
  });

  it('should return false for "no"', async () => {
    const createRL = createMockRL(['no']);
    const result = await promptYesNo('Continue?', true, createRL);
    expect(result).toBe(false);
  });

  it('should return defaultYes=true when input is empty', async () => {
    const createRL = createMockRL(['']);
    const result = await promptYesNo('Continue?', true, createRL);
    expect(result).toBe(true);
  });

  it('should return defaultYes=false when input is empty', async () => {
    const createRL = createMockRL(['']);
    const result = await promptYesNo('Continue?', false, createRL);
    expect(result).toBe(false);
  });
});

describe('promptGroupId', () => {
  it('should return default value when input is empty', async () => {
    const createRL = createMockRL(['']);
    const result = await promptGroupId('New groupId', 'com.example', createRL);
    expect(result).toBe('com.example');
  });

  it('should return user input when provided', async () => {
    const createRL = createMockRL(['com.lavaclone']);
    const result = await promptGroupId('New groupId', 'com', createRL);
    expect(result).toBe('com.lavaclone');
  });
});

describe('runInteractiveMode', () => {
  // Sequence: oldPackage, newPackage, oldGroupId, newGroupId, dryRun, verify

  it('should collect all fields and accept default groupIds', async () => {
    // Enter to accept derived groupIds
    const createRL = createMockRL(['top.flobby.admin', 'com.example.demo', '', '', 'n', 'n']);
    const config = await runInteractiveMode(createRL);
    expect(config.oldPackage).toBe('top.flobby.admin');
    expect(config.newPackage).toBe('com.example.demo');
    expect(config.oldGroupId).toBe('top.flobby');
    expect(config.newGroupId).toBe('com.example');
    expect(config.dryRun).toBe(false);
    expect(config.verify).toBe(false);
  });

  it('should allow overriding groupIds', async () => {
    const createRL = createMockRL(['top.flobby.admin', 'com.lavaclone', 'top.flobby', 'com.lavaclone', 'n', 'n']);
    const config = await runInteractiveMode(createRL);
    expect(config.oldPackage).toBe('top.flobby.admin');
    expect(config.newPackage).toBe('com.lavaclone');
    expect(config.oldGroupId).toBe('top.flobby');
    expect(config.newGroupId).toBe('com.lavaclone');
  });

  it('should set dryRun and verify to true when user answers yes', async () => {
    const createRL = createMockRL(['com.old.pkg', 'com.new.pkg', '', '', 'y', 'y']);
    const config = await runInteractiveMode(createRL);
    expect(config.dryRun).toBe(true);
    expect(config.verify).toBe(true);
  });

  it('should retry when old and new package names are the same', async () => {
    const createRL = createMockRL(['com.example.app', 'com.example.app', 'com.example.demo', '', '', 'n', 'n']);
    const config = await runInteractiveMode(createRL);
    expect(config.oldPackage).toBe('com.example.app');
    expect(config.newPackage).toBe('com.example.demo');
  });

  it('should retry on invalid package name format in interactive flow', async () => {
    const createRL = createMockRL(['BAD', 'com.valid.old', 'com.valid.new', '', '', '', '']);
    const config = await runInteractiveMode(createRL);
    expect(config.oldPackage).toBe('com.valid.old');
    expect(config.newPackage).toBe('com.valid.new');
  });

  it('should use default values for dryRun (no) and verify (no) on empty input', async () => {
    const createRL = createMockRL(['com.old.pkg', 'com.new.pkg', '', '', '', '']);
    const config = await runInteractiveMode(createRL);
    expect(config.dryRun).toBe(false);
    expect(config.verify).toBe(false);
  });
});
