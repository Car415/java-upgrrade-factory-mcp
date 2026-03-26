package com.company.upgradefactory.scanner.maven;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class PomScanner {

    public Optional<Model> scan(Path pomPath) {
        if (pomPath == null || !Files.exists(pomPath)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(pomPath)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            return Optional.of(pomReader.read(reader));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
