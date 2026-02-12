import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { scanBackendDirectory } from '../src/scanner.js';
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as os from 'node:os';

function createTempDir(): string {
  return fs.mkdtempSync(path.join(os.tmpdir(), 'scanner-test-'));
}

function cleanup(dir: string): void {
  fs.rmSync(dir, { recursive: true, force: true });
}

function createFile(dir: string, relativePath: string, content = ''): void {
  const fullPath = path.join(dir, relativePath);
  fs.mkdirSync(path.dirname(fullPath), { recursive: true });
  fs.writeFileSync(fullPath, content);
}

describe('scanBackendDirectory', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    cleanup(tempDir);
  });

  describe('discovers files by type', () => {
    it('should discover .java files', () => {
      createFile(tempDir, 'admin-boot/src/main/java/com/example/App.java', 'package com.example;');
      createFile(tempDir, 'admin-system/src/main/java/com/example/Svc.java', 'package com.example;');

      const result = scanBackendDirectory(tempDir);
      const javaFiles = result.filter(f => f.endsWith('.java'));
      expect(javaFiles).toHaveLength(2);
    });

    it('should discover pom.xml files', () => {
      createFile(tempDir, 'pom.xml', '<project></project>');
      createFile(tempDir, 'admin-boot/pom.xml', '<project></project>');

      const result = scanBackendDirectory(tempDir);
      const pomFiles = result.filter(f => f.endsWith('pom.xml'));
      expect(pomFiles).toHaveLength(2);
    });

    it('should discover .yml files', () => {
      createFile(tempDir, 'admin-boot/src/main/resources/application.yml', 'spring:');

      const result = scanBackendDirectory(tempDir);
      const ymlFiles = result.filter(f => f.endsWith('.yml'));
      expect(ymlFiles).toHaveLength(1);
    });

    it('should discover .xml files (non-pom)', () => {
      createFile(tempDir, 'admin-boot/src/main/resources/logback.xml', '<configuration></configuration>');

      const result = scanBackendDirectory(tempDir);
      const xmlFiles = result.filter(f => f.endsWith('.xml') && !f.endsWith('pom.xml'));
      expect(xmlFiles).toHaveLength(1);
    });

    it('should discover .properties files', () => {
      createFile(tempDir, 'admin-boot/src/main/resources/app.properties', 'key=value');

      const result = scanBackendDirectory(tempDir);
      const propFiles = result.filter(f => f.endsWith('.properties'));
      expect(propFiles).toHaveLength(1);
    });

    it('should return paths relative to the backend directory', () => {
      createFile(tempDir, 'admin-boot/src/main/java/com/example/App.java', 'package com.example;');

      const result = scanBackendDirectory(tempDir);
      expect(result[0]).toBe(path.join('admin-boot', 'src', 'main', 'java', 'com', 'example', 'App.java'));
    });
  });

  describe('respects exclusion patterns', () => {
    it('should exclude target/ directories', () => {
      createFile(tempDir, 'admin-boot/target/classes/com/example/App.class', '');
      createFile(tempDir, 'admin-boot/src/main/java/com/example/App.java', 'package com.example;');

      const result = scanBackendDirectory(tempDir);
      expect(result.some(f => f.includes('target'))).toBe(false);
      expect(result).toHaveLength(1);
    });

    it('should exclude .idea/ directories', () => {
      createFile(tempDir, '.idea/workspace.xml', '<project></project>');
      createFile(tempDir, 'pom.xml', '<project></project>');

      const result = scanBackendDirectory(tempDir);
      expect(result.some(f => f.includes('.idea'))).toBe(false);
    });

    it('should exclude node_modules/ directories', () => {
      createFile(tempDir, 'node_modules/some-pkg/index.js', '');
      createFile(tempDir, 'pom.xml', '<project></project>');

      const result = scanBackendDirectory(tempDir);
      expect(result.some(f => f.includes('node_modules'))).toBe(false);
    });

    it('should exclude .ftl files', () => {
      createFile(tempDir, 'admin-generator/src/main/resources/template.ftl', 'template');
      createFile(tempDir, 'pom.xml', '<project></project>');

      const result = scanBackendDirectory(tempDir);
      expect(result.some(f => f.endsWith('.ftl'))).toBe(false);
    });
  });

  describe('handles empty directories', () => {
    it('should return empty array for empty directory', () => {
      const result = scanBackendDirectory(tempDir);
      expect(result).toEqual([]);
    });

    it('should return empty array for directory with only excluded files', () => {
      createFile(tempDir, 'target/classes/App.class', '');
      createFile(tempDir, '.idea/workspace.xml', '');

      const result = scanBackendDirectory(tempDir);
      expect(result).toEqual([]);
    });
  });
});
