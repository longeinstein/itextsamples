package com.itextpdf.examples;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class FillFlattenMerge3 {

    public static final String[] FIELDS = {
        "name", "abbr", "capital", "city", "population", "surface", "timezone1", "timezone2", "dst"
    };
    public static final Font FONT = new Font(FontFamily.HELVETICA, 10);
    
    protected Map<String, Rectangle> positions;


    public class Background extends PdfPageEventHelper {

        PdfImportedPage background;
        
        public Background(PdfImportedPage background) {
            this.background = background;
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.addTemplate(background, 0, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase("page " + writer.getPageNumber()), 550, 800, 0);
        }
        
    }

    public byte[] manipulatePdf(InputStream is1, InputStream is2) throws DocumentException, IOException {
        PdfReader reader = new PdfReader(is1);
        AcroFields form = reader.getAcroFields();
        positions = new HashMap<String, Rectangle>();
        Rectangle rectangle;
        Map<String, AcroFields.Item> fields = form.getFields();
        for (String name : fields.keySet()) {
            rectangle = form.getFieldPositions(name).get(0).position;
            positions.put(name, rectangle);
        }
        // step 1
        Document document = new Document();
        // step 2
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setPageEvent(new Background(writer.getImportedPage(reader, 1)));
        // step 3
        document.open();
        // step 4
        PdfContentByte cb = writer.getDirectContent();
        StringTokenizer tokenizer;
        BufferedReader br = new BufferedReader(new InputStreamReader(is2));
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            int i = 0;
            tokenizer = new StringTokenizer(line, ";");
            while (tokenizer.hasMoreTokens()) {
                process(cb, FIELDS[i++], tokenizer.nextToken());
            }
            document.newPage();
        }
        br.close();
        // step 5
        document.close();
        
        reader.close();
        return output.toByteArray();
    }
    
    protected void process(PdfContentByte cb, String name, String value) throws DocumentException {
        Rectangle rect = positions.get(name);
        Phrase p = new Phrase(value, FONT);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                p, rect.getLeft() + 2, rect.getBottom() + 2, 0);
    }
}