package com.itheima.util;

import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.read.BaseSaxAnalyser;
import com.alibaba.excel.read.context.AnalysisContext;
import com.alibaba.excel.read.exception.ExcelAnalysisException;
import com.alibaba.excel.read.v07.RowHandler;
import com.alibaba.excel.read.v07.XMLTempFile;
import com.alibaba.excel.read.v07.XmlParserFactory;
import com.alibaba.excel.util.FileUtil;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class SaxAnalyserV07 extends BaseSaxAnalyser {

    private SharedStringsTable sharedStringsTable;

    private List<String> sharedStringList = new LinkedList<String>();

    private List<SheetSource> sheetSourceList = new ArrayList<SheetSource>();

    private boolean use1904WindowDate = false;

    private final String path;

    private File tmpFile;

    private String workBookXMLFilePath;

    private String sharedStringXMLFilePath;

    public SaxAnalyserV07(AnalysisContext analysisContext) throws Exception {
        this.analysisContext = analysisContext;
        this.path = XMLTempFile.createPath();
        this.tmpFile = new File(XMLTempFile.getTmpFilePath(path));
        this.workBookXMLFilePath = XMLTempFile.getWorkBookFilePath(path);
        this.sharedStringXMLFilePath = XMLTempFile.getSharedStringFilePath(path);
        start();
    }

    @Override
    protected void execute() {
        try {
            Sheet sheet = analysisContext.getCurrentSheet();
            if (!isAnalysisAllSheets(sheet)) {
                if (this.sheetSourceList.size() < sheet.getSheetNo() || sheet.getSheetNo() == 0) {
                    return;
                }
                InputStream sheetInputStream = this.sheetSourceList.get(sheet.getSheetNo() - 1).getInputStream();
                parseXmlSource(sheetInputStream);
                return;
            }
            int i = 0;
            for (SheetSource sheetSource : this.sheetSourceList) {
                i++;
                this.analysisContext.setCurrentSheet(new Sheet(i));
                parseXmlSource(sheetSource.getInputStream());
            }

        } catch (Exception e) {
            stop();
            throw new ExcelAnalysisException(e);
        } finally {
        }

    }

    private boolean isAnalysisAllSheets(Sheet sheet) {
        if (sheet == null) {
            return true;
        }
        if (sheet.getSheetNo() < 0) {
            return true;
        }
        return false;
    }

    public void stop() {
        FileUtil.deletefile(path);
    }

    private void parseXmlSource(InputStream inputStream) {
        try {
            ContentHandler handler = new RowHandler(this, this.sharedStringsTable, this.analysisContext,
                    sharedStringList);
            XmlParserFactory.parse(inputStream, handler);
            inputStream.close();
        } catch (Exception e) {
            try {
                inputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            throw new ExcelAnalysisException(e);
        }
    }

    public List<Sheet> getSheets() {
        List<Sheet> sheets = new ArrayList<Sheet>();
        try {
            int i = 1;
            for (SheetSource sheetSource : this.sheetSourceList) {
                Sheet sheet = new Sheet(i, 0);
                sheet.setSheetName(sheetSource.getSheetName());
                i++;
                sheets.add(sheet);
            }
        } catch (Exception e) {
            stop();
            throw new ExcelAnalysisException(e);
        } finally {

        }

        return sheets;
    }

    private void start() throws IOException, XmlException, ParserConfigurationException, SAXException {

        createTmpFile();

        unZipTempFile();

        initSharedStringsTable();

        initUse1904WindowDate();

        initSheetSourceList();

    }

    private void createTmpFile() throws FileNotFoundException {
        FileUtil.writeFile(tmpFile, analysisContext.getInputStream());
    }

    private void unZipTempFile() throws IOException {
        FileUtil.doUnZip(path, tmpFile);
    }

    private void initSheetSourceList() throws IOException, ParserConfigurationException, SAXException {
        this.sheetSourceList = new ArrayList<SheetSource>();
        InputStream workbookXml = new FileInputStream(this.workBookXMLFilePath);
        XmlParserFactory.parse(workbookXml, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
                if (qName.toLowerCase(Locale.US).equals("sheet")) {
                    String name = null;
                    int id = 0;
                    for (int i = 0; i < attrs.getLength(); i++) {
                        if (attrs.getLocalName(i).toLowerCase(Locale.US).equals("name")) {
                            name = attrs.getValue(i);
                        }/** else if (attrs.getLocalName(i).toLowerCase(Locale.US).equals("r:id")) {
                         id = Integer.parseInt(attrs.getValue(i).replaceAll("rId", ""));
                         try {
                         InputStream inputStream = new FileInputStream(XMLTempFile.getSheetFilePath(path, id));
                         sheetSourceList.add(new SheetSource(id, name, inputStream));
                         } catch (FileNotFoundException e) {
                         e.printStackTrace();
                         }
                         } **/
                        //应该使用sheetId属性
                        else if (attrs.getLocalName(i).toLowerCase(Locale.US).equals("sheetid")) {
                            id = Integer.parseInt(attrs.getValue(i));
                            try {
                                InputStream inputStream = new FileInputStream(XMLTempFile.getSheetFilePath(path, id));
                                sheetSourceList.add(new SheetSource(id, name, inputStream));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }

        });
        workbookXml.close();
        // 排序后是倒序，不符合实际要求
        // Collections.sort(sheetSourceList);
        Collections.sort(sheetSourceList, new Comparator<SheetSource>() {
            @Override
            public int compare(SheetSource o1, SheetSource o2) {
                return o1.id - o2.id;
            }
        });
    }

    private void initUse1904WindowDate() throws IOException, XmlException {
        InputStream workbookXml = new FileInputStream(workBookXMLFilePath);
        WorkbookDocument ctWorkbook = WorkbookDocument.Factory.parse(workbookXml);
        CTWorkbook wb = ctWorkbook.getWorkbook();
        CTWorkbookPr prefix = wb.getWorkbookPr();
        if (prefix != null) {
            this.use1904WindowDate = prefix.getDate1904();
        }
        this.analysisContext.setUse1904WindowDate(use1904WindowDate);
        workbookXml.close();
    }

    private void initSharedStringsTable() throws IOException, ParserConfigurationException, SAXException {
        //因为sharedStrings.xml文件不一定存在，所以在处理之前增加判断
        File sharedStringXMLFile = new File(this.sharedStringXMLFilePath);
        if (!sharedStringXMLFile.exists()) {
            return;
        }
        InputStream inputStream = new FileInputStream(this.sharedStringXMLFilePath);
        //this.sharedStringsTable = new SharedStringsTable();
        //this.sharedStringsTable.readFrom(inputStream);

        XmlParserFactory.parse(inputStream, new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) {
                sharedStringList.add(new String(ch, start, length));
            }

        });
        inputStream.close();
    }

    private class SheetSource implements Comparable<SheetSource> {

        private int id;

        private String sheetName;

        private InputStream inputStream;

        public SheetSource(int id, String sheetName, InputStream inputStream) {
            this.id = id;
            this.sheetName = sheetName;
            this.inputStream = inputStream;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int compareTo(SheetSource o) {
            if (o.id == this.id) {
                return 0;
            } else if (o.id > this.id) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}