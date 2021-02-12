package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceScanVisitor implements TypeElementVisitor<ResourceScan, Object> {

    private static final String RESOURCES_DIR = "src/main/resources";

    // Order of extensions has been chosen as a best fit for inter process dependencies.
    private static final List<String> VALID_EXTENSIONS = Arrays.asList(".dmn", ".cmmn", ".bpmn");

    @Override
    public void start(VisitorContext visitorContext) {
        System.out.println(" here I am !!!!!!!!!!!\n\n!!!!!!!");
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        System.err.println(element.getSimpleName());
        //TODO: Versuchen @ResourceScan in Factory zu annotieren  --> Prio 1
        //TODO: Gradle Demon Problem

        //TODO: Problem: Der Visitor wird nicht immer aufgerufen, wenn ein Model hinzukommt oder entfernt wird.
        // Ursache: Incremental Compilation !?
        Path projectDir = context.getProjectDir().get() ;

        try {
            Path resourcesRoot = projectDir.resolve(RESOURCES_DIR);
            final List<String> paths = Files.walk(resourcesRoot)
                    .filter(Files::isRegularFile)
                    .filter(p -> VALID_EXTENSIONS.stream().anyMatch(e -> p.toString().endsWith(e)))
                    .map( p -> resourcesRoot.relativize(p).toString())
                    .collect(Collectors.toList());
            //Path fileFilenames = Paths.get(projectDir.toString(), "build/resources/main", "mn.txt");
            /*
            Path fileFilenames = Paths.get(projectDir.toString(), "src/main/resources", "mn.txt");
            fileFilenames.getParent().toFile().mkdirs();
            try {
                Files.write(fileFilenames, paths, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            //context.addGeneratedResource(file.getFileName().toString());

            //element.annotate(ResourceScan.class, resourceScanAnnotationValueBuilder -> resourceScanAnnotationValueBuilder.member("models", paths.toArray(new String[paths.size()])));
            element.annotate(ResourceScan.class, resourceScanAnnotationValueBuilder -> resourceScanAnnotationValueBuilder.member("models", paths.toArray(new String[0])));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


