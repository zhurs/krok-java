package krok;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import com.beust.jcommander.JCommander;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Cli {
    private static final String FOP_CONF_XML = "fop.conf.xml";
    private final CliParameters params;

    private Cli(CliParameters params) {
        this.params = params;
    }

    public static void main(String[] args) {
        CliParameters params = new CliParameters();
        JCommander.newBuilder()
                .addObject(params)
                .build()
                .parse(args);
        new Cli(params).run();
    }

    private void run() {
        KrokData krokData = new KrokGenerator(params.pages, params.number, params.seed)
                .generate();

        String xml = processTemplate(krokData);

        makePdf(xml);
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

    private void makePdf(String xml) {
        File conf = new File(KrokGenerator.class.getClassLoader().getResource(FOP_CONF_XML).getFile());

        try {
            FopFactory fopFactory = FopFactory.newInstance(conf);

            try (
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(params.outputFile)))
            )
            {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer(); // identity transformer

                //File fo = new File(KrokGenerator.class.getClassLoader().getResource("template.xml").getFile());
                Source src = new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

                Result res = new SAXResult(fop.getDefaultHandler());

                transformer.transform(src, res);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
