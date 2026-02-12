import { describe, it, expect } from 'vitest';
import { validateAndCreateMapping } from '../src/validator.js';

describe('validateAndCreateMapping', () => {
  describe('valid package names', () => {
    it('should accept standard 3-segment package names', () => {
      const result = validateAndCreateMapping('top.flobby.admin', 'com.example.demo');
      expect(result.mapping).toBeDefined();
      expect(result.error).toBeUndefined();
      expect(result.mapping!.oldPackage).toBe('top.flobby.admin');
      expect(result.mapping!.newPackage).toBe('com.example.demo');
    });

    it('should accept 2-segment package names (minimum)', () => {
      const result = validateAndCreateMapping('com.example', 'org.test');
      expect(result.mapping).toBeDefined();
      expect(result.error).toBeUndefined();
    });

    it('should accept package names with numbers in non-first position', () => {
      const result = validateAndCreateMapping('com.example2.app', 'org.test3.demo');
      expect(result.mapping).toBeDefined();
      expect(result.error).toBeUndefined();
    });

    it('should accept long package names', () => {
      const result = validateAndCreateMapping('com.example.project.module.sub', 'org.company.app.core.impl');
      expect(result.mapping).toBeDefined();
      expect(result.error).toBeUndefined();
    });
  });

  describe('invalid formats', () => {
    it('should reject uppercase letters', () => {
      const result = validateAndCreateMapping('com.Example.demo', 'com.example.demo');
      expect(result.error).toBeDefined();
      expect(result.mapping).toBeUndefined();
    });

    it('should reject special characters', () => {
      const result = validateAndCreateMapping('com.ex-ample.demo', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should reject segments starting with digit', () => {
      const result = validateAndCreateMapping('com.2example.demo', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should reject single segment (no dot)', () => {
      const result = validateAndCreateMapping('example', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should reject empty string', () => {
      const result = validateAndCreateMapping('', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should reject trailing dot', () => {
      const result = validateAndCreateMapping('com.example.', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should reject leading dot', () => {
      const result = validateAndCreateMapping('.com.example', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should reject consecutive dots', () => {
      const result = validateAndCreateMapping('com..example', 'com.example.demo');
      expect(result.error).toBeDefined();
    });

    it('should validate new package name too', () => {
      const result = validateAndCreateMapping('com.example.demo', 'Com.Example');
      expect(result.error).toBeDefined();
    });
  });

  describe('same old/new rejection', () => {
    it('should reject when old and new package names are identical', () => {
      const result = validateAndCreateMapping('com.example.demo', 'com.example.demo');
      expect(result.error).toBeDefined();
      expect(result.error).toContain('same');
    });
  });

  describe('groupId derivation', () => {
    it('should derive groupId by dropping last segment', () => {
      const result = validateAndCreateMapping('top.flobby.admin', 'com.example.demo');
      expect(result.mapping!.oldGroupId).toBe('top.flobby');
      expect(result.mapping!.newGroupId).toBe('com.example');
    });

    it('should derive groupId from 2-segment package', () => {
      const result = validateAndCreateMapping('com.example', 'org.test');
      expect(result.mapping!.oldGroupId).toBe('com');
      expect(result.mapping!.newGroupId).toBe('org');
    });

    it('should derive groupId from long package', () => {
      const result = validateAndCreateMapping('com.example.project.module', 'org.company.app.core');
      expect(result.mapping!.oldGroupId).toBe('com.example.project');
      expect(result.mapping!.newGroupId).toBe('org.company.app');
    });
  });

  describe('path derivation', () => {
    it('should convert package dots to directory separators', () => {
      const result = validateAndCreateMapping('top.flobby.admin', 'com.example.demo');
      expect(result.mapping!.oldPath).toBe('top/flobby/admin');
      expect(result.mapping!.newPath).toBe('com/example/demo');
    });
  });
});
