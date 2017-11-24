
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import static java.nio.file.Files.*;

public class FileSynch {

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || args.length != 2) {
            System.out.println("Attention this application required two directories like arguments in command line.");
            System.exit(0);
        }
        Path sourcePath = Paths.get(args[0]);
        if (!exists(sourcePath)) {
            System.out.println(String.format("Attention the first argument '%s' does not found!", sourcePath.toAbsolutePath()));
            System.exit(0);
        }
        if (!isDirectory(sourcePath)) {
            System.out.println(String.format("The first parameter '%s' is not a directory!", sourcePath));
            System.exit(0);
        }
        Path targetPath = Paths.get(args[1]);
        if (exists(targetPath)) {
            if (isDirectory(targetPath)) {
                System.out.println(String.format("We are going to delete files from '%s' these even deleted from '%s'", targetPath, sourcePath));
                deleteFromTarget(targetPath, sourcePath);
            }
        }
        System.out.println(String.format("Start coping files from '%s' to '%s'", sourcePath, targetPath));
        copyFromSource(sourcePath, targetPath);
        System.out.println("--");
        System.out.println("Operation is complete.");
    }

    private static void deleteFromTarget(Path sourcePath, Path targetPath) throws IOException {
        Files.walkFileTree(sourcePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path resolve = targetPath.resolve(sourcePath.relativize(file));
                if (!exists(resolve)) {
                    Files.delete(file);
                    System.out.println(String.format("\tFile '%s' deleted", file));
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Path target = targetPath.resolve(sourcePath.relativize(dir));
                if (!exists(target)) {
                    Files.delete(dir);
                    System.out.println(String.format("\tDirectory '%s' deleted'", dir));
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void copyFromSource(Path sourcePath, Path targetPath) throws IOException {
        Files.walkFileTree(sourcePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException  {
                Path target = targetPath.resolve(sourcePath.relativize(dir));
                try {
                    if (!exists(target)) {
                        Files.copy(dir, target);
                        System.out.println(String.format("\tDirectory '%s' created'", target));
                    } else {
                        System.out.println(String.format("\tDirectory '%s' exists like '%s'", dir, target));
                    }
                } catch (FileAlreadyExistsException e) {
                    if (!Files.isDirectory(target))
                        throw e;
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path resolve = targetPath.resolve(sourcePath.relativize(file));
                if (!exists(resolve)) {
                    Files.copy(file, resolve);
                    System.out.println(String.format("\tFile '%s' copied like '%s'",file, resolve));
                } else {
                    if (Files.size(file) != Files.size(resolve)) {
                        Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println(String.format("\tFile '%s' updated like '%s'", file, resolve));
                    } else {
                        System.out.println(String.format("\tFile '%s' skipped",file));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
