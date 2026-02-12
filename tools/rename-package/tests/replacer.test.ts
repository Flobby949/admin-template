import { describe, it, expect } from 'vitest';
import { replaceJavaContent, replacePomContent, replaceConfigContent, detectWarnings } from '../src/replacer.js';
import type { PackageMapping } from '../src/types.js';

const mapping: PackageMapping = {
  oldPackage: 'top.flobby.admin',
  newPackage: 'com.example.demo',
  oldGroupId: 'top.flobby',
  newGroupId: 'com.example',
  oldPath: 'top/flobby/admin',
  newPath: 'com/example/demo',
};

describe('replaceJavaContent', () => {
  describe('package declaration replacement', () => {
    it('should replace package declaration with exact match', () => {
      const content = 'package top.flobby.admin;\n\npublic class App {}';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('package com.example.demo;\n\npublic class App {}');
      expect(result.changes).toHaveLength(1);
      expect(result.changes[0].changeType).toBe('PACKAGE_DECLARATION');
    });

    it('should replace package declaration with subpackage', () => {
      const content = 'package top.flobby.admin.system.interfaces.query;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('package com.example.demo.system.interfaces.query;\n');
    });

    it('should replace package declaration in admin-boot config subpackage', () => {
      const content = 'package top.flobby.admin.config;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('package com.example.demo.config;\n');
    });
  });

  describe('import statement replacement', () => {
    it('should replace import with matching prefix', () => {
      const content = 'import top.flobby.admin.common.core.PageQuery;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('import com.example.demo.common.core.PageQuery;\n');
      expect(result.changes[0].changeType).toBe('IMPORT_STATEMENT');
    });

    it('should replace multiple imports', () => {
      const content = [
        'package top.flobby.admin.config;',
        '',
        'import org.springframework.context.annotation.Bean;',
        'import top.flobby.admin.security.JwtAccessDeniedHandler;',
        'import top.flobby.admin.security.JwtAuthenticationEntryPoint;',
        'import top.flobby.admin.security.JwtAuthenticationFilter;',
        '',
        'public class SecurityConfig {}',
      ].join('\n');

      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toContain('import com.example.demo.security.JwtAccessDeniedHandler;');
      expect(result.newContent).toContain('import com.example.demo.security.JwtAuthenticationEntryPoint;');
      expect(result.newContent).toContain('import com.example.demo.security.JwtAuthenticationFilter;');
      // 1 package + 3 imports = 4 changes
      expect(result.changes).toHaveLength(4);
    });

    it('should preserve non-matching imports', () => {
      const content = [
        'import org.springframework.context.annotation.Bean;',
        'import org.springframework.context.annotation.Configuration;',
        'import lombok.Data;',
      ].join('\n');

      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe(content);
      expect(result.changes).toHaveLength(0);
    });
  });

  describe('handle subpackages', () => {
    it('should replace subpackage imports correctly', () => {
      const content = 'import top.flobby.admin.system.domain.entity.User;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('import com.example.demo.system.domain.entity.User;\n');
    });

    it('should replace generator subpackage', () => {
      const content = 'package top.flobby.admin.generator;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('package com.example.demo.generator;\n');
    });

    it('should not replace partial match that is not a subpackage', () => {
      const content = 'import top.flobby.adminer.something;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe(content);
      expect(result.changes).toHaveLength(0);
    });
  });

  describe('edge cases', () => {
    it('should handle file with no matching content', () => {
      const content = 'package com.other.project;\n\npublic class Foo {}';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe(content);
      expect(result.changes).toHaveLength(0);
    });

    it('should handle static imports', () => {
      const content = 'import static top.flobby.admin.common.util.Constants.MAX;\n';
      const result = replaceJavaContent(content, mapping);
      expect(result.newContent).toBe('import static com.example.demo.common.util.Constants.MAX;\n');
    });
  });
});

describe('replacePomContent', () => {
  describe('groupId exact match replacement', () => {
    it('should replace project groupId in parent POM', () => {
      const content = [
        '    <groupId>top.flobby</groupId>',
        '    <artifactId>admin-parent</artifactId>',
      ].join('\n');

      const result = replacePomContent(content, mapping);
      expect(result.newContent).toContain('<groupId>com.example</groupId>');
      expect(result.changes.some(c => c.changeType === 'GROUP_ID')).toBe(true);
    });

    it('should replace parent reference groupId', () => {
      const content = [
        '    <parent>',
        '        <groupId>top.flobby</groupId>',
        '        <artifactId>admin-parent</artifactId>',
        '        <version>1.0.0-SNAPSHOT</version>',
        '    </parent>',
      ].join('\n');

      const result = replacePomContent(content, mapping);
      expect(result.newContent).toContain('<groupId>com.example</groupId>');
    });

    it('should replace internal dependency groupId', () => {
      const content = [
        '        <dependency>',
        '            <groupId>top.flobby</groupId>',
        '            <artifactId>admin-system</artifactId>',
        '            <version>${project.version}</version>',
        '        </dependency>',
      ].join('\n');

      const result = replacePomContent(content, mapping);
      expect(result.newContent).toContain('<groupId>com.example</groupId>');
    });
  });

  describe('external dependency groupId preserved', () => {
    it('should NOT replace Spring Boot groupId', () => {
      const content = [
        '        <dependency>',
        '            <groupId>org.springframework.boot</groupId>',
        '            <artifactId>spring-boot-starter-web</artifactId>',
        '        </dependency>',
      ].join('\n');

      const result = replacePomContent(content, mapping);
      expect(result.newContent).toBe(content);
      expect(result.changes).toHaveLength(0);
    });

    it('should NOT replace MySQL groupId', () => {
      const content = '<groupId>com.mysql</groupId>';
      const result = replacePomContent(content, mapping);
      expect(result.newContent).toBe(content);
    });

    it('should NOT replace Lombok groupId', () => {
      const content = '<groupId>org.projectlombok</groupId>';
      const result = replacePomContent(content, mapping);
      expect(result.newContent).toBe(content);
    });

    it('should handle mixed internal and external dependencies', () => {
      const content = [
        '    <dependencies>',
        '        <dependency>',
        '            <groupId>top.flobby</groupId>',
        '            <artifactId>admin-system</artifactId>',
        '            <version>${project.version}</version>',
        '        </dependency>',
        '        <dependency>',
        '            <groupId>org.springframework.boot</groupId>',
        '            <artifactId>spring-boot-starter-web</artifactId>',
        '        </dependency>',
        '    </dependencies>',
      ].join('\n');

      const result = replacePomContent(content, mapping);
      expect(result.newContent).toContain('<groupId>com.example</groupId>');
      expect(result.newContent).toContain('<groupId>org.springframework.boot</groupId>');
      expect(result.changes).toHaveLength(1);
    });
  });

  describe('mainClass replacement', () => {
    it('should replace mainClass in admin-boot pom.xml', () => {
      const content = '<mainClass>top.flobby.admin.AdminApplication</mainClass>';
      const result = replacePomContent(content, mapping);
      expect(result.newContent).toBe('<mainClass>com.example.demo.AdminApplication</mainClass>');
      expect(result.changes.some(c => c.changeType === 'MAIN_CLASS')).toBe(true);
    });

    it('should replace mainClass in admin-generator pom.xml', () => {
      const content = '<mainClass>top.flobby.admin.generator.GeneratorApplication</mainClass>';
      const result = replacePomContent(content, mapping);
      expect(result.newContent).toBe('<mainClass>com.example.demo.generator.GeneratorApplication</mainClass>');
    });

    it('should handle both groupId and mainClass in same file', () => {
      const content = [
        '    <parent>',
        '        <groupId>top.flobby</groupId>',
        '        <artifactId>admin-parent</artifactId>',
        '    </parent>',
        '    <build>',
        '        <plugins>',
        '            <configuration>',
        '                <mainClass>top.flobby.admin.AdminApplication</mainClass>',
        '            </configuration>',
        '        </plugins>',
        '    </build>',
      ].join('\n');

      const result = replacePomContent(content, mapping);
      expect(result.newContent).toContain('<groupId>com.example</groupId>');
      expect(result.newContent).toContain('<mainClass>com.example.demo.AdminApplication</mainClass>');
      const groupIdChanges = result.changes.filter(c => c.changeType === 'GROUP_ID');
      const mainClassChanges = result.changes.filter(c => c.changeType === 'MAIN_CLASS');
      expect(groupIdChanges.length).toBeGreaterThanOrEqual(1);
      expect(mainClassChanges.length).toBeGreaterThanOrEqual(1);
    });
  });
});

describe('replaceConfigContent', () => {
  describe('yml value replacement', () => {
    it('should replace package name in yml config', () => {
      const content = 'packageName: top.flobby.admin\nother: value';
      const result = replaceConfigContent(content, mapping);
      expect(result.newContent).toBe('packageName: com.example.demo\nother: value');
      expect(result.changes).toHaveLength(1);
      expect(result.changes[0].changeType).toBe('CONFIG_REFERENCE');
    });

    it('should replace subpackage references in yml', () => {
      const content = 'basePackage: top.flobby.admin.generator';
      const result = replaceConfigContent(content, mapping);
      expect(result.newContent).toBe('basePackage: com.example.demo.generator');
    });
  });

  describe('xml attribute replacement', () => {
    it('should replace logger name in logback.xml', () => {
      const content = '<logger name="top.flobby.admin.generator" level="DEBUG"/>';
      const result = replaceConfigContent(content, mapping);
      expect(result.newContent).toBe('<logger name="com.example.demo.generator" level="DEBUG"/>');
    });

    it('should replace multiple references in xml', () => {
      const content = [
        '<logger name="top.flobby.admin" level="INFO"/>',
        '<logger name="top.flobby.admin.generator" level="DEBUG"/>',
      ].join('\n');
      const result = replaceConfigContent(content, mapping);
      expect(result.newContent).toContain('name="com.example.demo"');
      expect(result.newContent).toContain('name="com.example.demo.generator"');
      expect(result.changes).toHaveLength(2);
    });
  });

  describe('properties value replacement', () => {
    it('should replace package name in properties', () => {
      const content = 'base.package=top.flobby.admin\nother.key=other.value';
      const result = replaceConfigContent(content, mapping);
      expect(result.newContent).toBe('base.package=com.example.demo\nother.key=other.value');
    });
  });

  describe('preserve non-matching content', () => {
    it('should not modify content without old package name', () => {
      const content = 'spring:\n  application:\n    name: admin-system';
      const result = replaceConfigContent(content, mapping);
      expect(result.newContent).toBe(content);
      expect(result.changes).toHaveLength(0);
    });
  });
});

describe('detectWarnings', () => {
  it('should detect string literals containing old package name', () => {
    const content = [
      'package top.flobby.admin.util;',
      '',
      'public class ReflectionUtil {',
      '    public void load() {',
      '        Class.forName("top.flobby.admin.SomeClass");',
      '    }',
      '}',
    ].join('\n');

    const warnings = detectWarnings(content, 'util/ReflectionUtil.java', mapping);
    expect(warnings).toHaveLength(1);
    expect(warnings[0].line).toBe(5);
    expect(warnings[0].filePath).toBe('util/ReflectionUtil.java');
    expect(warnings[0].reason).toContain('String literal');
  });

  it('should not flag package and import lines', () => {
    const content = [
      'package top.flobby.admin.util;',
      'import top.flobby.admin.common.Result;',
      '',
      'public class Foo {}',
    ].join('\n');

    const warnings = detectWarnings(content, 'test.java', mapping);
    expect(warnings).toHaveLength(0);
  });

  it('should detect multiple warnings in same file', () => {
    const content = [
      'package top.flobby.admin;',
      'public class Foo {',
      '    String a = "top.flobby.admin.X";',
      '    String b = "top.flobby.admin.Y";',
      '}',
    ].join('\n');

    const warnings = detectWarnings(content, 'Foo.java', mapping);
    expect(warnings).toHaveLength(2);
  });

  it('should return empty for files without string literal matches', () => {
    const content = [
      'package top.flobby.admin;',
      'public class Clean {',
      '    String name = "hello";',
      '}',
    ].join('\n');

    const warnings = detectWarnings(content, 'Clean.java', mapping);
    expect(warnings).toHaveLength(0);
  });
});
