import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { moveDirectories } from '../src/mover.js';
import type { PackageMapping } from '../src/types.js';
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as os from 'node:os';

const mapping: PackageMapping = {
  oldPackage: 'top.flobby.admin',
  newPackage: 'com.example.demo',
  oldGroupId: 'top.flobby',
  newGroupId: 'com.example',
  oldPath: 'top/flobby/admin',
  newPath: 'com/example/demo',
};

function createTempDir(): string {
  return fs.mkdtempSync(path.join(os.tmpdir(), 'mover-test-'));
}

function cleanup(dir: string): void {
  fs.rmSync(dir, { recursive: true, force: true });
}

function createFile(dir: string, relativePath: string, content = ''): void {
  const fullPath = path.join(dir, relativePath);
  fs.mkdirSync(path.dirname(fullPath), { recursive: true });
  fs.writeFileSync(fullPath, content);
}

function fileExists(dir: string, relativePath: string): boolean {
  return fs.existsSync(path.join(dir, relativePath));
}

function readFile(dir: string, relativePath: string): string {
  return fs.readFileSync(path.join(dir, relativePath), 'utf-8');
}

describe('moveDirectories', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    cleanup(tempDir);
  });

  describe('directory creation', () => {
    it('should create new directory structure', () => {
      createFile(
        tempDir,
        'admin-boot/src/main/java/top/flobby/admin/AdminApplication.java',
        'package top.flobby.admin;'
      );

      moveDirectories(tempDir, mapping);

      expect(fileExists(tempDir, 'admin-boot/src/main/java/com/example/demo/AdminApplication.java')).toBe(true);
    });

    it('should create nested new directories', () => {
      createFile(
        tempDir,
        'admin-system/src/main/java/top/flobby/admin/system/domain/entity/User.java',
        'package top.flobby.admin.system.domain.entity;'
      );

      moveDirectories(tempDir, mapping);

      expect(fileExists(tempDir, 'admin-system/src/main/java/com/example/demo/system/domain/entity/User.java')).toBe(true);
    });
  });

  describe('file move via renameSync', () => {
    it('should move files preserving content', () => {
      const originalContent = 'package top.flobby.admin;\n\npublic class AdminApplication {}';
      createFile(
        tempDir,
        'admin-boot/src/main/java/top/flobby/admin/AdminApplication.java',
        originalContent
      );

      moveDirectories(tempDir, mapping);

      const movedContent = readFile(tempDir, 'admin-boot/src/main/java/com/example/demo/AdminApplication.java');
      expect(movedContent).toBe(originalContent);
    });

    it('should move files from multiple modules', () => {
      createFile(tempDir, 'admin-boot/src/main/java/top/flobby/admin/App.java', 'content1');
      createFile(tempDir, 'admin-system/src/main/java/top/flobby/admin/Svc.java', 'content2');
      createFile(tempDir, 'admin-common/src/main/java/top/flobby/admin/Util.java', 'content3');

      const changes = moveDirectories(tempDir, mapping);

      expect(fileExists(tempDir, 'admin-boot/src/main/java/com/example/demo/App.java')).toBe(true);
      expect(fileExists(tempDir, 'admin-system/src/main/java/com/example/demo/Svc.java')).toBe(true);
      expect(fileExists(tempDir, 'admin-common/src/main/java/com/example/demo/Util.java')).toBe(true);
      expect(changes.length).toBeGreaterThanOrEqual(3);
    });

    it('should return DIRECTORY_MOVE change records', () => {
      createFile(tempDir, 'admin-boot/src/main/java/top/flobby/admin/App.java', 'content');

      const changes = moveDirectories(tempDir, mapping);

      expect(changes.some(c => c.changeType === 'DIRECTORY_MOVE')).toBe(true);
    });
  });

  describe('empty directory cleanup (bottom-up)', () => {
    it('should clean up empty old directories after move', () => {
      createFile(tempDir, 'admin-boot/src/main/java/top/flobby/admin/App.java', 'content');

      moveDirectories(tempDir, mapping);

      // Old directories should be cleaned up
      expect(fileExists(tempDir, 'admin-boot/src/main/java/top/flobby/admin')).toBe(false);
      expect(fileExists(tempDir, 'admin-boot/src/main/java/top/flobby')).toBe(false);
      expect(fileExists(tempDir, 'admin-boot/src/main/java/top')).toBe(false);
    });

    it('should preserve src/main/java directory', () => {
      createFile(tempDir, 'admin-boot/src/main/java/top/flobby/admin/App.java', 'content');

      moveDirectories(tempDir, mapping);

      expect(fileExists(tempDir, 'admin-boot/src/main/java')).toBe(true);
    });
  });

  describe('non-empty old dir preserved with warning', () => {
    it('should preserve old directory if it still contains files', () => {
      createFile(tempDir, 'admin-boot/src/main/java/top/flobby/admin/App.java', 'content');
      createFile(tempDir, 'admin-boot/src/main/java/top/flobby/admin/.gitkeep', '');

      const changes = moveDirectories(tempDir, mapping);

      // New location should exist
      expect(fileExists(tempDir, 'admin-boot/src/main/java/com/example/demo/App.java')).toBe(true);
      // Old directory still has .gitkeep, so parent should remain
      expect(fileExists(tempDir, 'admin-boot/src/main/java/top/flobby/admin/.gitkeep')).toBe(true);
    });
  });

  describe('test directories', () => {
    it('should also move files in src/test/java paths', () => {
      createFile(tempDir, 'admin-system/src/test/java/top/flobby/admin/system/UserTest.java', 'test content');

      moveDirectories(tempDir, mapping);

      expect(fileExists(tempDir, 'admin-system/src/test/java/com/example/demo/system/UserTest.java')).toBe(true);
    });
  });

  describe('no matching directories', () => {
    it('should return empty changes when no old package directories exist', () => {
      createFile(tempDir, 'admin-boot/src/main/java/com/other/App.java', 'content');

      const changes = moveDirectories(tempDir, mapping);

      expect(changes).toHaveLength(0);
    });
  });
});
