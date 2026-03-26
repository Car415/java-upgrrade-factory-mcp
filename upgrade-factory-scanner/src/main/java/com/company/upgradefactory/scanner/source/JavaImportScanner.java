package com.company.upgradefactory.scanner.source;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JavaImportScanner {

    public List<String> findImports(Path sourceRoot, String prefix) throws IOException {
        List<String> matches = new ArrayList<>();
        if (sourceRoot == null || !Files.exists(sourceRoot)) {
            return matches;
        }
        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> parseAndCollect(path, prefix, matches));
        }
        return matches;
    }

    private void parseAndCollect(Path path, String prefix, List<String> matches) {
        try {
            CompilationUnit unit = StaticJavaParser.parse(path);
            unit.getImports().stream()
                    .map(importDeclaration -> importDeclaration.getNameAsString())
                    .filter(name -> name.startsWith(prefix))
                    .forEach(matches::add);
        } catch (Exception ignored) {
            // tolerate parse failures in MVP
        }
    }
}
