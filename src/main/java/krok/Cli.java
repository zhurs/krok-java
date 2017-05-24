package krok;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Cli {
    private static final Logger logger = LoggerFactory.getLogger(Cli.class);

    private static final String FOP_CONF_XML = "fop.conf.xml";
    public static final String[] MEDIA_FILES = {
            "logo_big.gif"
    };

    private final CliParameters params;

    private Cli(CliParameters params) {
        this.params = params;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        CliParameters params = new CliParameters();
        JCommander.newBuilder()
                .addObject(params)
                .build()
                .parse(args);
        new Cli(params).run();
    }

    private void run() throws IOException {
        KrokData krokData = new KrokGenerator(params.pages, params.number, params.seed)
                .generate();

        String xml = processTemplate(krokData);

        makePdf(xml);

        logger.info("File {} created", params.outputFile);
    }

    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.XML);
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".xml");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    private String processTemplate(KrokData krok) {
        Context context = new Context();
        context.setVariable("krok", krok);
        context.setVariable("params", params);
        context.setVariable("LETTERS", KrokUtil.LETTERS);
        context.setVariable("NUMS", KrokUtil.NUMS);

        StringWriter stringWriter = new StringWriter();
        createTemplateEngine().process("template", context, stringWriter);
        return stringWriter.toString();
    }

    private void makePdf(String xml) throws IOException {
        Path tempDir = makeWorkingDirectory();

        try (
                ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                InputStream confStream = getResource(FOP_CONF_XML).openStream();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(params.outputFile)));
        )
        {
            Fop fop = FopFactory.newInstance(tempDir.toUri(), confStream)
                    .newFop(MimeConstants.MIME_PDF, out);
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();

            Source src = new StreamSource(xmlStream);
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    private URL getResource(String path) {
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new IllegalArgumentException("Resource " + path + " not found");
        }
        return url;
    }

    private Path makeWorkingDirectory() {
        try {
            Path tempDir = Files.createTempDirectory("krok-gen");
            logger.info("temporary directory: {}", tempDir);
            try {
                for (String mediaFile : MEDIA_FILES) {
                    try (InputStream inputStream = getResource(mediaFile).openStream()) {
                        Files.copy(inputStream, tempDir.resolve(mediaFile));
                    }
                }
            } catch (Exception e) {
                FileUtils.deleteDirectory(tempDir.toFile());
                throw e;
            }
            return tempDir;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
