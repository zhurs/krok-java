package krok;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
    private final CliParameters params;

    public Cli(CliParameters params) {
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
        KrokData krokData = new KrokGenerator(params.pages, getKrokNumber(), params.seed).generate();

        String xml = processTemplate(krokData);

        makePdf(xml);
    }

    private int getKrokNumber() {
        if (params.number > 0) {
            return params.number;
        } else {
            LocalDateTime now = LocalDateTime.now();
            int shift = now.getMonthValue() > 10 || now.getMonthValue() < 6
                    ? 1
                    : 2;
            return 2 * (now.getYear() - 1996) + shift;
        }
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
        StringWriter stringWriter = new StringWriter();

        Context context = new Context();
        context.setVariable("krok", krok);
        context.setVariable("params", params);
        context.setVariable("LETTERS", KrokUtil.LETTERS);
        context.setVariable("NUMS", KrokUtil.NUMS);

        createTemplateEngine().process("template", context, stringWriter);
        return stringWriter.toString();
    }

    private static void makePdf(String xml) {
        File conf = new File(KrokGenerator.class.getClassLoader().getResource("fop.conf.xml").getFile());

        try {
            FopFactory fopFactory = FopFactory.newInstance(conf);

            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("/Users/krok/ret.pdf")));

            try {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer(); // identity transformer

                //File fo = new File(KrokGenerator.class.getClassLoader().getResource("template.xml").getFile());
                Source src = new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

                Result res = new SAXResult(fop.getDefaultHandler());

                transformer.transform(src, res);
            } finally {
                out.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
