/*
 * Copyright 2016 Jin Kwon &lt;onacit_at_gmail.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jinahya.wadl.bind;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.java.dev.wadl._2009._02.Application;
import net.java.dev.wadl._2009._02.Resource;
import net.java.dev.wadl._2009._02.Resources;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.testng.annotations.Test;

/**
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
public class UnmarshalTest {

    private static final Logger logger = getLogger(UnmarshalTest.class);

    private static final Path PATH;

    static {
        try {
            PATH = new File(UnmarshalTest.class.getResource("/").toURI())
                    .toPath();
        } catch (final URISyntaxException urise) {
            throw new InstantiationError(urise.toString());
        }
    }

    private void resource(final Resource resource) {
        logger.debug("resource.path: {}", resource.getPath());
    }

    private void resources(final Resources resources) {
        resources.getResource().forEach(this::resource);
    }

    @Test
    public void test() throws JAXBException, IOException {
        final JAXBContext context
                = JAXBContext.newInstance(Application.class);
        Files.walkFileTree(
                PATH,
                new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs)
                    throws IOException {
                if (file.getFileName().getName(0).toString().endsWith(".wadl")) {
                    try {
                        final Unmarshaller unmarshaller
                                = context.createUnmarshaller();
                        final Application application
                                = (Application) unmarshaller.unmarshal(
                                        file.toFile());
                        logger.debug("unmarshalled: {}", application);
                        application.getResources().forEach(UnmarshalTest.this::resources);
                    } catch (final JAXBException jaxbe) {
                        logger.error("failed to unmarshal {}", file, jaxbe);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
