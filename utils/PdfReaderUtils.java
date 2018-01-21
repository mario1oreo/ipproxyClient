//package com.fosun.financial.data.proxy.ipproxyclient.utils;
//
//import com.xiaoleilu.hutool.collection.CollUtil;
//import com.xiaoleilu.hutool.poi.excel.ExcelUtil;
//import com.xiaoleilu.hutool.poi.excel.ExcelWriter;
//import com.xiaoleilu.hutool.util.ReUtil;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.pdfbox.pdfparser.PDFParser;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.util.PDFTextStripper;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Pattern;
//
///**
// * 人行征信报告简版PDF  临时解析查询记录
// *
// * @author mario1oreo
// * @date 2018-1-10 14:51:47
// */
//public class PdfReaderUtils {
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        String pdfPath = "D:/creditReport/pdf/";
//        String txtfilePath = "D:/creditReport/excel/";
//        PdfReaderUtils pdfutil = new PdfReaderUtils();
//        File pdfRootPath = new File(pdfPath);
//        if (pdfRootPath.isDirectory()) {
//            File[] pdfs = pdfRootPath.listFiles();
//            for (File pdf : pdfs) {
//                try {
//                    String content = pdfutil.getTextFromPdf(pdf);
//                    pdfutil.toTextFile(content, txtfilePath + pdf.getName().split("\\.")[0] + ".xlsx");
//                    System.out.println("Finished !");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//
//    }
//
//    /**
//     * 读取PDF文件的文字内容
//     *
//     * @param pdfFile
//     * @throws Exception
//     */
//    public String getTextFromPdf(File pdfFile) throws Exception {
//        // 是否排序
//        boolean sort = false;
//        // 开始提取页数
//        int startPage = 1;
//        // 结束提取页数
//        int endPage = Integer.MAX_VALUE;
//
//        String content = null;
//        InputStream input = null;
//        PDDocument document = null;
//        try {
//            input = new FileInputStream(pdfFile);
//            // 加载 pdf 文档
//            PDFParser parser = new PDFParser(input);
//            parser.parse();
//            document = parser.getPDDocument();
//            // 获取内容信息
//            PDFTextStripper pts = new PDFTextStripper();
//            pts.setSortByPosition(sort);
//            endPage = document.getNumberOfPages();
//            System.out.println("Total Page: " + endPage);
//            pts.setStartPage(startPage);
//            pts.setEndPage(endPage);
//            try {
//                content = pts.getText(document);
//            } catch (Exception e) {
//                throw e;
//            }
//            System.out.println("Get PDF Content ...");
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            if (null != input)
//                input.close();
//            if (null != document)
//                document.close();
//        }
//
//        return content;
//    }
//
//    /**
//     * 把PDF文件内容写入到txt文件中
//     *
//     * @param pdfContent
//     * @param filePath
//     */
//    public void toTextFile(String pdfContent, String filePath) {
//        try {
//            File f = new File(filePath);
//            if (!f.exists()) {
//                f.createNewFile();
//            }
//            System.out.println("Write PDF Content to txt file ...");
//            try (BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pdfContent.getBytes())))) {
//                String line;
//                String rpt_id = "";
//                String rpt_dt = "";
//                String cust_nm = "";
//                String regexIDDT = "报告编号\\S (.*)查询时间\\S (.*)报告时间\\S?(.*)";
////                String regexIDDT = "报告编号： (.*) 查询时间：(.*) 报告时间：(.*)";
//                //自己生成PDF
//                String regexIDDTSelf = "报告编号： (.*) 查询时间：(.*) 报告时间：(.*)";
//                Pattern patternIDDT = Pattern.compile(regexIDDT);
//                String regexNM = "姓名\\S? (.*) 证件类型: 身份证 证件号码: ";
//                String regexNMSelf = "姓名： (.*) 证件类型：身份证 证件号码：";
//                Pattern patternNM = Pattern.compile(regexNM);
//                String regexLOG = "(\\d+) (.*) (.*) (.*)";
//                Pattern patternLOG = Pattern.compile(regexLOG);
//                String lastContent = "";
//                String fileName = f.getName().split("\\.")[0];
//                List<List<String>> contentList = new ArrayList<>();
//                contentList.add(CollUtil.newArrayList("file", "rpt_id", "rpt_dt", "cust_nm", "qry_dt", "qry_org", "qry_reason"));
//                String qry_dt = "";
//                String qry_org = "";
//                String qry_reason = "";
//                while ((line = input.readLine()) != null) {
//                        System.out.println("line = " + line);
//                    if (StringUtils.isEmpty(rpt_id) && ReUtil.get(patternIDDT, line, 1) != null) {
//                        rpt_id = ReUtil.get(patternIDDT, line, 1);
//                        rpt_dt = ReUtil.get(patternIDDT, line, 2);
//                    } else if (StringUtils.isEmpty(cust_nm) && ReUtil.get(patternNM, line, 1) != null) {
//                        cust_nm = ReUtil.get(patternNM, line, 1);
////                        System.out.println("line = " + line);
//                    } else if (line.contains("机构查询记录明细") || line.contains("本人查询记录明细")) {
//                        lastContent = line;
//                        System.out.println("lastContent = " + lastContent);
//                    } else if (line.equals("说  明")) {
////                        lastContent = "";
////                        break;
//                    } else if (lastContent.contains("机构查询记录明细") || lastContent.contains("本人查询记录明细")) {
//
//                        if (lastContent.contains("本人查询记录明细") && "）".equals(line) && StringUtils.isNotEmpty(qry_reason)) {
//                            qry_reason += "）";
//                            contentList.add(CollUtil.newArrayList(fileName, rpt_id, rpt_dt, cust_nm, qry_dt, qry_org, qry_reason));
//                            qry_dt = "";
//                            qry_org = "";
//                            qry_reason = "";
//                        } else if (ReUtil.get(patternLOG, line, 1) != null) {
//                            if (StringUtils.isNotEmpty(qry_dt)) {
//                                contentList.add(CollUtil.newArrayList(fileName, rpt_id, rpt_dt, cust_nm, qry_dt, qry_org, qry_reason));
//                                qry_dt = "";
//                                qry_org = "";
//                                qry_reason = "";
//                            }
//                            qry_dt = ReUtil.get(patternLOG, line, 2);
//                            qry_org = ReUtil.get(patternLOG, line, 3);
//                            qry_reason = ReUtil.get(patternLOG, line, 4);
//                        }
//                    }
//                }
//                for (List<String> strings : contentList) {
//                    System.out.println("strings = " + strings.toString());
//                }
//                //通过工具类创建writer
//                ExcelWriter writer = ExcelUtil.getWriter(filePath);
////                    //一次性写出内容
//                writer.write(contentList);
////                    //关闭writer，释放内存
//                writer.close();
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}