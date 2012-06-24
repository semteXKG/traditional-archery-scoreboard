/**********************************************************************************************************************
 * ReportGenerator
 * 
 * created Mar 18, 2012 by semteX
 * 
 * (c) 2012 APEX gaming technology GmbH
 **********************************************************************************************************************/

package semtex.archery.entities.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import semtex.archery.entities.data.entities.Parcour;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Version;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.util.Log;

import com.j256.ormlite.dao.GenericRawResults;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * @author semteX
 * 
 */
public class ReportGenerator {

  private static final String TAG = ReportGenerator.class.getName();

  private final DatabaseHelper daoHelper;

  private final DateFormat dateFormatter = DateFormat.getDateInstance();

  private static Font titleFont = FontFactory.getFont(FontFactory.COURIER, 28, Font.BOLD);

  private static Font timeFont = FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD);

  private static Font timeFont2 = FontFactory.getFont(FontFactory.COURIER, 14);

  private static Font tableFont = FontFactory.getFont(FontFactory.COURIER, 12);

  private static Font tableFontBold = FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD);


  public ReportGenerator(final DatabaseHelper daoHelper) {
    this.daoHelper = daoHelper;
  }


  public ParcourReportData generateReportForVisit(final Visit visit) {
    Log.i(TAG, "Beginning calculation");
    final ParcourReportData reportData = new ParcourReportData();

    final Map<String, Integer> totalNumbers = new HashMap<String, Integer>();
    final Map<String, Integer> totalPoints = new HashMap<String, Integer>();
    daoHelper.getVersionDao().refresh(visit.getVersion());
    final Version version = visit.getVersion();
    daoHelper.getParcourDao().refresh(version.getParcour());
    final Parcour parcour = version.getParcour();

    reportData.setParcourName(parcour.getName());
    reportData.setParcourRevisionDate(version.getCreated());
    Log.i(TAG, "STARTING NEW");
    final GenericRawResults<String[]> queryRaw =
        daoHelper.getParcourDao().queryRaw(
            "SELECT u.userName, target.target_number, target_hit.points FROM visit "
                + "LEFT JOIN version ON visit.version_id = version.id "
                + "LEFT JOIN target ON target.version= version.id "
                + "LEFT JOIN target_hit ON target_hit.target = target.id "
                + "LEFT JOIN user_visit uv ON target_hit.user = uv.id " + "LEFT JOIN user u ON uv.user_id = u.id "
                + "WHERE visit.id=" + visit.getId() + " AND uv.visit_id=" + visit.getId()
                + " ORDER BY target.target_number");
    // new DataType[] { DataType.STRING, DataType.INTEGER_OBJ, DataType.INTEGER_OBJ
    final Map<Integer, Map<String, Integer>> scoringData = reportData.getScoringData();

    for (final String[] objects : queryRaw) {
      final String userName = objects[0];
      final Integer targetNumber = Integer.valueOf(objects[1]);
      final Integer points = objects[2] != null ? Integer.valueOf(objects[2]) : null;
      if (points != null) {
        Map<String, Integer> targetHitMap = scoringData.get(targetNumber);
        if (targetHitMap == null) {
          targetHitMap = new HashMap<String, Integer>();
          scoringData.put(targetNumber, targetHitMap);
        }
        targetHitMap.put(userName, points);
        totalNumbers.put(userName, safeGet(totalNumbers, userName) + 1);
        totalPoints.put(userName, safeGet(totalPoints, userName) + points);
      }
    }

    final Map<String, Double> avgPoints = new HashMap<String, Double>();

    for (final Map.Entry<String, Integer> totalNumberEntry : totalNumbers.entrySet()) {
      final double avgCalcPoints = totalPoints.get(totalNumberEntry.getKey()) * 1.0 / totalNumberEntry.getValue();
      avgPoints.put(totalNumberEntry.getKey(), avgCalcPoints);
    }

    reportData.setAvgPoints(avgPoints);
    reportData.setTotalPoints(totalPoints);
    Log.i(TAG, "Ending calculation");
    return reportData;
  }


  public String generateHTMLReportForVisit(final Visit visit) {
    final ParcourReportData data = generateReportForVisit(visit);
    final StringBuilder builder = new StringBuilder();
    builder.append("<h1>" + visit.getVersion().getParcour().getName() + "</h2><br>");
    builder.append("<h4>" + visit.getBeginTime() + "</h4><br>");
    builder.append("<b>" + String.format("%10s", "Archers:") + "</b>");
    Iterator<UserVisit> it = visit.getUserVisit().iterator();
    while (it.hasNext()) {
      final UserVisit uv = it.next();
      builder.append(String.format("%10s", uv.getUser().getUserName()) + (it.hasNext() ? ", " : ""));
    }

    builder.append("<br><b>" + String.format("%10s", "total") + "</b>");
    it = visit.getUserVisit().iterator();
    while (it.hasNext()) {
      final UserVisit uv = it.next();
      builder.append(String.format("%10s", data.getTotalPoints().get(uv.getUser().getUserName()))
          + (it.hasNext() ? ", " : ""));
    }

    builder.append("<br><b>" + String.format("%10s", "avg") + "</b>");
    it = visit.getUserVisit().iterator();
    while (it.hasNext()) {
      final UserVisit uv = it.next();
      final Double value = data.getAvgPoints().get(uv.getUser().getUserName());
      builder.append(String.format("%10s", value != null ? MessageFormat.format("{0,number,#.##}", value) : "-")
          + (it.hasNext() ? ", " : ""));
    }

    for (final Map.Entry<Integer, Map<String, Integer>> entry : data.getScoringData().entrySet()) {
      builder.append("<br><b>" + String.format("%10s", entry.getKey()) + "</b> | ");
      it = visit.getUserVisit().iterator();
      while (it.hasNext()) {
        final UserVisit uv = it.next();
        builder.append(String.format("%10s", entry.getValue().get(uv.getUser().getUserName()) != null ? entry
            .getValue().get(uv.getUser().getUserName()) : "-")
            + (it.hasNext() ? ", " : ""));
      }
    }
    return builder.toString();
  }


  public File generatePDFReportForVisit(final Visit visit) throws DocumentException, IOException {
    final ParcourReportData data = generateReportForVisit(visit);

    final Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
    final File file = new File(ExternalStorageManager.getApplicationPath(), File.separator + visit.getId() + ".pdf");
    final FileOutputStream fos =
        new FileOutputStream(new File(ExternalStorageManager.getApplicationPath(), File.separator + visit.getId()
            + ".pdf"));
    PdfWriter.getInstance(doc, fos);
    doc.open();
    Paragraph p = new Paragraph();
    p.add(new Chunk(visit.getVersion().getParcour().getName(), titleFont));
    p.setAlignment(Element.ALIGN_CENTER);
    doc.add(p);
    // new line
    doc.add(new Paragraph(" "));

    p = new Paragraph();
    p.add(new Chunk("Begin: ", timeFont));
    p.add(new Chunk(visit.getBeginTime().toLocaleString() + "\n", timeFont2));
    p.add(new Chunk("End:   ", timeFont));
    if (visit.getEndTime() != null) {
      p.add(new Chunk(visit.getEndTime().toLocaleString(), timeFont2));
    }

    doc.add(p);

    doc.add(new Paragraph(" "));

    final PdfPTable table = new PdfPTable(visit.getUserVisit().size() + 1);
    PdfPCell cell = new PdfPCell();
    cell.setBorderWidthBottom(2);

    table.addCell(cell);
    for (final UserVisit uv : visit.getUserVisit()) {
      cell = new PdfPCell(new Phrase(uv.getUser().getUserName(), tableFontBold));
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      cell.setBorderWidthBottom(2);
      table.addCell(cell);
    }

    cell = new PdfPCell(new Phrase("total", tableFontBold));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(cell);
    for (final UserVisit uv : visit.getUserVisit()) {
      cell = new PdfPCell(new Phrase(data.getTotalPoints().get(uv.getUser().getUserName()).toString(), tableFont));
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      table.addCell(cell);
    }

    cell = new PdfPCell(new Phrase("avg", tableFontBold));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setBorderWidthBottom(2);
    table.addCell(cell);

    for (final UserVisit uv : visit.getUserVisit()) {
      final Double value = data.getAvgPoints().get(uv.getUser().getUserName());
      cell = new PdfPCell(new Phrase(value != null ? MessageFormat.format("{0,number,#.##}", value) : "-", tableFont));
      cell.setBorderWidthBottom(2);
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      table.addCell(cell);
    }

    for (final Map.Entry<Integer, Map<String, Integer>> entry : data.getScoringData().entrySet()) {
      cell = new PdfPCell(new Phrase(new Phrase(entry.getKey().toString(), tableFontBold)));
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(cell);
      for (final UserVisit uv : visit.getUserVisit()) {
        cell =
            new PdfPCell(new Phrase(entry.getValue().get(uv.getUser().getUserName()) != null ? entry.getValue()
                .get(uv.getUser().getUserName()).toString() : "-", tableFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
      }
    }
    doc.add(table);

    doc.close();
    fos.close();
    return file;
    // return null;
  }


  private int safeGet(final Map<String, Integer> map, final String key) {
    final Integer val = map.get(key);
    if (val == null) {
      return 0;
    }
    return val;
  }


  private double safeGetDouble(final Map<String, Double> map, final String key) {
    final Double val = map.get(key);
    if (val == null) {
      return 0;
    }
    return val;
  }
}
