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
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.java.dev.wadl._2009._02.Application;
import net.java.dev.wadl._2009._02.Method;
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

    private static final Path ROOT;

    static {
        final URL url = UnmarshalTest.class.getResource("/");
        try {
            ROOT = new File(url.toURI()).toPath();
        } catch (final URISyntaxException urise) {
            throw new InstantiationError(urise.toString());
        }
    }

    private void method(final Method method) {
        logger.debug("method.id: {}", method.getId());
        logger.debug("method.name: {}", method.getName());
        logger.debug("method.href: {}", method.getHref());
    }

    private void resource(final Resource resource) {
        logger.debug("resource.id: {}", resource.getId());
        logger.debug("resource.type: {}", resource.getType());
        logger.debug("resource.queryType: {}", resource.getQueryType());
        logger.debug("resource.path: {}", resource.getPath());
        resource.getParam().forEach(v -> {
            logger.debug("param.id", v.getId());
            logger.debug("param.name: {}", v.getName());
            logger.debug("param.style: {}", v.getStyle());
            logger.debug("param.fixed: {}", v.getFixed());
        });
        resource.getMethodOrResource().forEach(v -> {
            if (v instanceof Resource) {
                resource((Resource) v);
            } else if (v instanceof Method) {
                method((Method) v);
            }
        });
    }

    private void resources(final Resources resources) {
        logger.debug("resources.base: {}", resources.getBase());
        resources.getResource().forEach(this::resource);
    }

    @Test
    public void test() throws JAXBException, IOException {
        final JAXBContext context
                = JAXBContext.newInstance(Application.class);
        Files.walkFileTree(
                ROOT,
                new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs)
                    throws IOException {
                final String name = file.getFileName().getName(0).toString();
                if (name.endsWith(".wadl")) {
                    try {
                        final Unmarshaller unmarshaller
                                = context.createUnmarshaller();
                        final Application application
                                = (Application) unmarshaller.unmarshal(
                                        file.toFile());
                        logger.debug("unmarshalled: {}", application);
                        application.getResources().forEach(
                                UnmarshalTest.this::resources);
                    } catch (final JAXBException jaxbe) {
                        logger.error("failed to unmarshal {}", file, jaxbe);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
