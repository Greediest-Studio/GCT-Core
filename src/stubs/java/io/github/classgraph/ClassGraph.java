package io.github.classgraph;

public class ClassGraph {
    public ClassGraph acceptPackages(String... packageNames) {
        return this;
    }

    public ClassGraph overrideClasspath(Iterable<?> classpathElements) {
        return this;
    }

    public ClassGraph disableNestedJarScanning() {
        return this;
    }

    public ClassGraph disableModuleScanning() {
        return this;
    }
}
