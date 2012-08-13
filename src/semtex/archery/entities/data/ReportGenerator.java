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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import semtex.archery.entities.data.entities.Parcour;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Version;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.JsonVisitReport;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.GenericRawResults;
import com.lowagie.text.*;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * @author semteX
 * 
 */
public class ReportGenerator {

  private static final int PDF_COLUMN_PADDING = 5;

  private static final String TAG = ReportGenerator.class.getName();

  private final DatabaseHelper daoHelper;

  private final DateFormat dateFormatter = DateFormat.getDateInstance();

  // private final DateFormat parcourVersionDateFormatter = new SimpleDateFormater("YYYY-MM-dd");

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  private static Font titleFont = FontFactory.getFont(FontFactory.COURIER, 28, Font.BOLD);

  private static Font timeFont = FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD);

  private static Font timeFont2 = FontFactory.getFont(FontFactory.COURIER, 14);

  private static Font tableFont = FontFactory.getFont(FontFactory.COURIER, 12);

  private static Font tableFontBold = FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD);

  private static final GrayColor evenBg = new GrayColor(200);

  private static final GrayColor oddBg = new GrayColor(230);


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
    reportData.setBeginTime(visit.getBeginTime());
    reportData.setEndTime(visit.getEndTime());

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
      } else {
        // this sanitation block will be used when there is no result for one user.
        if (totalNumbers.get(userName) == null) {
          totalNumbers.put(userName, 0);
        } // if
        if (totalPoints.get(userName) == null) {
          totalPoints.put(userName, 0);
        } // if
      }
    }

    final Map<String, Double> avgPoints = new HashMap<String, Double>();

    for (final Map.Entry<String, Integer> totalNumberEntry : totalNumbers.entrySet()) {
      double avgCalcPoints;

      // avoid division by zero
      if (totalNumberEntry.getValue() != 0) {
        avgCalcPoints = totalPoints.get(totalNumberEntry.getKey()) * 1.0 / totalNumberEntry.getValue();
      } else { // if
        avgCalcPoints = 0;
      } // else
      avgPoints.put(totalNumberEntry.getKey(), avgCalcPoints);
    } // for

    reportData.setAvgPoints(avgPoints);
    reportData.setTotalPoints(totalPoints);
    Log.i(TAG, "Ending calculation");
    return reportData;
  }


  public List<String> generateJsonObjectsForVisit(final Visit visit) {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDateFormat(DateFormat.getDateTimeInstance());
    final List<String> reports = new LinkedList<String>();
    final ParcourReportData data = generateReportForVisit(visit);
    for (final UserVisit uservisit : visit.getUserVisit()) {
      final JsonVisitReport report = new JsonVisitReport();
      report.setLabel(data.getParcourName() + " (" + sdf.format(data.getParcourRevisionDate()) + ")");
      report.setName(uservisit.getUser().getUserName());
      report.setVisitDate(data.getBeginTime());
      report.setParcourDate(data.getParcourRevisionDate());

      final SortedMap<Integer, Integer> scoreMap = new TreeMap<Integer, Integer>();
      scoreMap.put(0, 0);

      for (final Map.Entry<Integer, Map<String, Integer>> entry : data.getScoringData().entrySet()) {
        scoreMap.put(entry.getKey(), entry.getValue().get(uservisit.getUser().getUserName()));
      }
      report.setData(scoreMap);
      try {
        reports.add(objectMapper.writeValueAsString(report));
      } catch(final JsonGenerationException e) {
        Log.e(TAG, "error while serializing json", e);
      } catch(final JsonMappingException e) {
        Log.e(TAG, "error while serializing json", e);
      } catch(final IOException e) {
        Log.e(TAG, "error while serializing json", e);
      }
    }

    return reports;
  }


  public String generateHTMLReportForVisit(final Visit visit) {
    final ParcourReportData data = generateReportForVisit(visit);
    final StringBuilder builder = new StringBuilder();
    builder.append("<h1>" + visit.getVersion().getParcour().getName() + " (Version: "
        + dateFormatter.format(visit.getVersion().getCreated())
        + (visit.getVersion().getName() != null ? " - " + visit.getVersion().getName() : "") + "</h2><br>");

    builder.append("<h4>" + visit.getBeginTime() + " - " + visit.getEndTime() + "</h4><br>");
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

    builder.append("<br><b>" + String.format("%10s", "average") + "</b>");
    it = visit.getUserVisit().iterator();
    while (it.hasNext()) {
      final UserVisit uv = it.next();
      final Double value = data.getAvgPoints().get(uv.getUser().getUserName());
      builder.append(String.format("%10s", value != null ? MessageFormat.format("{0,number,#.##}", value) : "-")
          + (it.hasNext() ? ", " : ""));
    }

    final TreeSet<Integer> keySet = new TreeSet<Integer>(data.getScoringData().keySet());
    for (final Integer key : keySet) {
      final Map<String, Integer> entry = data.getScoringData().get(key);

      builder.append("<br><b>" + String.format("%10s", key) + "</b> | ");
      it = visit.getUserVisit().iterator();
      while (it.hasNext()) {
        final UserVisit uv = it.next();
        builder.append(String.format("%10s",
            entry.get(uv.getUser().getUserName()) != null ? entry.get(uv.getUser().getUserName()) : "-")
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

    generateNewLines(doc, 3);

    p = new Paragraph();
    p.add(new Chunk("Parcour created: ", timeFont));
    p.add(new Chunk(dateFormatter.format(visit.getVersion().getCreated()), timeFont2));
    doc.add(p);

    generateNewLines(doc, 1);

    p = new Paragraph();
    p.add(new Chunk("Begin: ", timeFont));
    p.add(new Chunk(visit.getBeginTime().toLocaleString() + "\n", timeFont2));
    p.add(new Chunk("End:   ", timeFont));
    if (visit.getEndTime() != null) {
      p.add(new Chunk(visit.getEndTime().toLocaleString(), timeFont2));
    }

    doc.add(p);

    generateNewLines(doc, 2);

    final PdfPTable table = new PdfPTable(visit.getUserVisit().size() + 1);
    PdfPCell cell = new PdfPCell();
    cell.setBorderWidthBottom(2);
    cell.setBackgroundColor(evenBg);

    table.addCell(cell);
    for (final UserVisit uv : visit.getUserVisit()) {
      cell = new PdfPCell(new Phrase(uv.getUser().getUserName(), tableFontBold));
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      cell.setBorderWidthBottom(2);
      cell.setBackgroundColor(evenBg);
      setCellPaddings(cell, PDF_COLUMN_PADDING);
      table.addCell(cell);
    }

    cell = new PdfPCell(new Phrase("total", tableFontBold));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setBackgroundColor(oddBg);
    setCellPaddings(cell, PDF_COLUMN_PADDING);
    table.addCell(cell);
    for (final UserVisit uv : visit.getUserVisit()) {
      cell = new PdfPCell(new Phrase(data.getTotalPoints().get(uv.getUser().getUserName()).toString(), tableFont));
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      setCellPaddings(cell, PDF_COLUMN_PADDING);
      cell.setBackgroundColor(oddBg);
      table.addCell(cell);
    }

    cell = new PdfPCell(new Phrase("avg", tableFontBold));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setBorderWidthBottom(2);
    cell.setBackgroundColor(evenBg);
    setCellPaddings(cell, PDF_COLUMN_PADDING);
    table.addCell(cell);
    for (final UserVisit uv : visit.getUserVisit()) {
      final Double value = data.getAvgPoints().get(uv.getUser().getUserName());
      cell = new PdfPCell(new Phrase(value != null ? MessageFormat.format("{0,number,#.##}", value) : "-", tableFont));
      cell.setBorderWidthBottom(2);
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      cell.setBackgroundColor(evenBg);
      setCellPaddings(cell, PDF_COLUMN_PADDING);
      table.addCell(cell);
    }

    int modCounter = 0;

    final TreeSet<Integer> keySet = new TreeSet<Integer>(data.getScoringData().keySet());

    for (final Integer key : keySet) {
      final Map<String, Integer> entry = data.getScoringData().get(key);
      cell = new PdfPCell(new Phrase(new Phrase(key.toString(), tableFontBold)));
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      cell.setBackgroundColor(modCounter % 2 == 0 ? oddBg : evenBg);
      table.addCell(cell);
      for (final UserVisit uv : visit.getUserVisit()) {
        cell =
            new PdfPCell(new Phrase(entry.get(uv.getUser().getUserName()) != null ? entry.get(
                uv.getUser().getUserName()).toString() : "-", tableFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBackgroundColor(modCounter % 2 == 0 ? oddBg : evenBg);
        table.addCell(cell);
      }
      modCounter++;
    }

    doc.add(table);

    doc.close();
    fos.close();
    return file;
  }


  private void setCellPaddings(final PdfPCell cell, final float cellPadding) {
    cell.setPaddingBottom(cellPadding);
    cell.setPaddingTop(cellPadding);
  }


  private void generateNewLines(final Document doc, final int lineCount) throws DocumentException {
    for (int i = 0; i < lineCount; i++) {
      doc.add(new Paragraph(" "));
    }
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
