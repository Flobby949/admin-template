import * as readline from 'node:readline';
import { validateAndCreateMapping, deriveGroupId } from './validator.js';

export type CreateRL = () => readline.Interface;

const defaultCreateRL: CreateRL = () =>
  readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

/**
 * Ask a single-line question and return the trimmed answer.
 */
function ask(rl: readline.Interface, question: string): Promise<string> {
  return new Promise((resolve) => {
    rl.question(question, (answer) => {
      resolve(answer.trim());
    });
  });
}

/**
 * Prompt the user for a valid Java package name.
 * Re-prompts on invalid input (no retry limit).
 */
export async function promptPackageName(label: string, createRL: CreateRL = defaultCreateRL): Promise<string> {
  while (true) {
    const rl = createRL();
    const answer = await ask(rl, `${label}: `);
    rl.close();

    if (!answer) {
      console.log('  Package name cannot be empty. Please try again.');
      continue;
    }

    // Validate format using a dummy counterpart
    const validation = validateAndCreateMapping(answer, 'zz.zz');
    if (validation.error && !validation.error.includes('same')) {
      console.log(`  ${validation.error}`);
      console.log('  Please try again.');
      continue;
    }

    return answer;
  }
}

/**
 * Prompt the user for a yes/no answer.
 * Returns defaultYes when input is empty.
 */
export async function promptYesNo(question: string, defaultYes: boolean, createRL: CreateRL = defaultCreateRL): Promise<boolean> {
  const hint = defaultYes ? '[Y/n]' : '[y/N]';
  const rl = createRL();
  const answer = await ask(rl, `${question} ${hint} `);
  rl.close();

  if (answer === '') {
    return defaultYes;
  }

  const normalized = answer.toLowerCase();
  return normalized === 'y' || normalized === 'yes';
}

/**
 * Prompt the user to confirm or override a groupId.
 * Shows the derived default; user can press Enter to accept or type a new value.
 */
export async function promptGroupId(label: string, defaultValue: string, createRL: CreateRL = defaultCreateRL): Promise<string> {
  const rl = createRL();
  const answer = await ask(rl, `${label} [${defaultValue}]: `);
  rl.close();

  if (!answer) {
    return defaultValue;
  }

  return answer;
}

export interface InteractiveConfig {
  oldPackage: string;
  newPackage: string;
  oldGroupId: string;
  newGroupId: string;
  dryRun: boolean;
  verify: boolean;
}

/**
 * Run the full interactive mode flow:
 * 1. Prompt old package name (with validation retry)
 * 2. Prompt new package name (with validation retry)
 * 3. Validate old != new (retry new if same)
 * 4. Prompt dry-run? (default: no)
 * 5. Prompt verify? (default: no)
 */
export async function runInteractiveMode(createRL: CreateRL = defaultCreateRL): Promise<InteractiveConfig> {
  console.log('\nPackage Rename Tool — Interactive Mode\n');

  // Step 1: Old package name
  const oldPackage = await promptPackageName('Old package name', createRL);

  // Step 2+3: New package name (with same-check retry)
  let newPackage: string;
  while (true) {
    newPackage = await promptPackageName('New package name', createRL);
    if (newPackage === oldPackage) {
      console.log('  Old and new package names are the same. Please enter a different package name.');
      continue;
    }
    break;
  }

  // Step 4: Confirm groupId mapping
  const derivedOldGroupId = deriveGroupId(oldPackage);
  const derivedNewGroupId = deriveGroupId(newPackage);
  console.log(`\nGroupId mapping (derived): ${derivedOldGroupId} -> ${derivedNewGroupId}`);
  const oldGroupId = await promptGroupId('Old groupId', derivedOldGroupId, createRL);
  const newGroupId = await promptGroupId('New groupId', derivedNewGroupId, createRL);

  // Step 5: Dry-run?
  const dryRun = await promptYesNo('Preview only (dry-run)?', false, createRL);

  // Step 6: Verify compile?
  const verify = await promptYesNo('Verify compile after execution?', false, createRL);

  return { oldPackage, newPackage, oldGroupId, newGroupId, dryRun, verify };
}
