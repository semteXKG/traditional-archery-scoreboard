
package semtex.archery;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.ForeignCollection;


public class Scoring extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = Scoring.class.getName();

  private Visit currentVisit;

  private Target currentTarget;

  private static final int MAX_ARROWS = 4;

  private final Map<UserVisit, TargetHit> userScoring = new HashMap<UserVisit, TargetHit>();

  private final Map<UserVisit, Integer> currentArrow = new HashMap<UserVisit, Integer>();

  private final int scoringMatrix[][] = new int[5][];

  private final String arrowBtnDesc[] = new String[5];


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scoring);

    initScoringMatrix();

    final Button btnNext = (Button)findViewById(R.id.btnNext);
    btnNext.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        saveResultsAndSwap(true);
      }
    });

    final Button btnLast = (Button)findViewById(R.id.btnPrev);
    btnLast.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        saveResultsAndSwap(false);
      }
    });
    fetchSetupData();
    updateUIElements();
  }


  private void initScoringMatrix() {
    scoringMatrix[1] = new int[3];
    scoringMatrix[1][0] = 20;
    scoringMatrix[1][1] = 18;
    scoringMatrix[1][2] = 16;

    scoringMatrix[2] = new int[3];
    scoringMatrix[2][0] = 14;
    scoringMatrix[2][1] = 12;
    scoringMatrix[2][2] = 10;

    scoringMatrix[3] = new int[3];
    scoringMatrix[3][0] = 8;
    scoringMatrix[3][1] = 6;
    scoringMatrix[3][2] = 4;

    scoringMatrix[4] = new int[3];
    scoringMatrix[4][0] = 0;
    scoringMatrix[4][1] = 0;
    scoringMatrix[4][2] = 0;

    arrowBtnDesc[1] = "-1-";
    arrowBtnDesc[2] = "-2-";
    arrowBtnDesc[3] = "-3-";
    arrowBtnDesc[4] = "-X-";

  }


  private void updateUIElements() {
    final TextView txtTargetNumber = (TextView)findViewById(R.id.txtTargetNumber);
    txtTargetNumber.setText("Target " + currentTarget.getTargetNumber());

    final ListView listView = (ListView)findViewById(R.id.lvUserScoring);
    final ArrayAdapter<UserVisit> adapter =
        new UserVisitAdapter(this, R.layout.scoring_user_row, new LinkedList(userScoring.keySet()));
    listView.setAdapter(adapter);
  }


  protected void saveResultsAndSwap(final boolean forward) {
    // Saving results to parcour page
    for (final TargetHit th : userScoring.values()) {
      if (th.getId() == null) {
        Log.i(TAG, "Saving TH info in DB " + th);
        getHelper().getTargetHitDao().create(th);
      } else {
        Log.i(TAG, "Updating TH info in DB " + th);
        getHelper().getTargetHitDao().update(th);
      }
    }

    // fetching the next target..
    final int nextTargetNumber = currentTarget.getTargetNumber() + (forward ? 1 : -1);
    if (nextTargetNumber < 1) {
      Toast.makeText(getApplicationContext(), "No Targets < 1", Toast.LENGTH_SHORT).show();
    } else {
      Target nextTarget =
          getHelper().getTargetDao().findTargetByTargetNumber(nextTargetNumber, currentVisit.getVersion());
      if (nextTarget == null) {
        Log.i(TAG, "Created new Target with number " + nextTargetNumber);
        nextTarget = new Target(nextTargetNumber, currentVisit.getVersion());
        getHelper().getTargetDao().create(nextTarget);
      }
      currentTarget = nextTarget;
    }

    fillTargetHitMap();
    updateUIElements();
  }


  private void fetchSetupData() {
    currentVisit = getHelper().getVisitDao().findLastOpenVisit();
    Log.i(TAG, "Found: " + currentVisit);
    currentTarget = getHelper().getTargetDao().findLastTarget(currentVisit.getVersion());
    Log.i(TAG, "current Target set to: " + currentTarget);

    fillTargetHitMap();
  }


  private void fillTargetHitMap() {
    userScoring.clear();

    final ForeignCollection<UserVisit> uv = currentVisit.getUserVisit();
    for (final UserVisit userVisit : uv) {
      final TargetHit th = getHelper().getTargetHitDao().findTargetHitByUserVisitAndTarget(userVisit, currentTarget);
      if (th != null) {
        Log.i(TAG, "TargetHit " + th + " found for " + userVisit);
        userScoring.put(userVisit, th);
      } else {
        Log.i(TAG, "Creating new TargetHit for " + userVisit);
        final TargetHit newHit = new TargetHit(0, 1, userVisit, currentTarget);
        userScoring.put(userVisit, newHit);
      }
    }
    Log.i(TAG, "added " + uv.size() + " players");
  }

  public class UserVisitAdapter extends ArrayAdapter<UserVisit> {

    public UserVisitAdapter(final Context context, final int textViewResourceId, final List<UserVisit> objects) {
      super(context, textViewResourceId, objects);
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      View rowView = convertView;
      final UserVisit uv = getItem(position);
      if (rowView == null) {
        final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        rowView = li.inflate(R.layout.scoring_user_row, null);
      }

      final View finalRowView = rowView;

      final TextView userName = (TextView)rowView.findViewById(R.id.txtUsername);
      userName.setText(uv.getUser().getUserName());

      final TextView points = (TextView)rowView.findViewById(R.id.txtPoints);
      points.setText("-");

      final Button btnArrows = (Button)rowView.findViewById(R.id.btnNoOfArrows);
      btnArrows.setText(arrowBtnDesc[userScoring.get(uv).getNrOfArrows()]);

      btnArrows.setOnClickListener(new View.OnClickListener() {

        public void onClick(final View v) {
          final int usedArrows = incrementUsedArrows();
          btnArrows.setText("-" + usedArrows + "-");
          updateHitButtons(finalRowView, usedArrows);
        }


        private int incrementUsedArrows() {
          final TargetHit th = userScoring.get(uv);
          final int usedArrows = th.getNrOfArrows() % MAX_ARROWS + 1;
          th.setNrOfArrows(usedArrows);
          return usedArrows;
        }

      });

      updateHitButtons(finalRowView, userScoring.get(uv).getNrOfArrows());

      // This is a total hack, ignore it for the time being, will be refactored ASAP
      final Button btnLow = (Button)rowView.findViewById(R.id.btnLow);
      btnLow.setOnClickListener(new View.OnClickListener() {

        public void onClick(final View v) {
          final TargetHit th = userScoring.get(uv);
          th.setPoints(scoringMatrix[th.getNrOfArrows()][2]);
        }
      });

      final Button btnMid = (Button)rowView.findViewById(R.id.btnMed);
      btnMid.setOnClickListener(new View.OnClickListener() {

        public void onClick(final View v) {
          final TargetHit th = userScoring.get(uv);
          th.setPoints(scoringMatrix[th.getNrOfArrows()][1]);
        }
      });
      final Button btnHigh = (Button)rowView.findViewById(R.id.btnHigh);
      btnHigh.setOnClickListener(new View.OnClickListener() {

        public void onClick(final View v) {
          final TargetHit th = userScoring.get(uv);
          th.setPoints(scoringMatrix[th.getNrOfArrows()][0]);
        }
      });

      final GradientDrawable gd =
          new GradientDrawable(Orientation.RIGHT_LEFT, new int[] { uv.getUser().getRgbColor() & 0x77FFFFFF, 0x0 });
      rowView.setBackgroundDrawable(gd);
      return rowView;
    }


    private void updateHitButtons(final View v, final int arr) {
      final Button btnArrow = (Button)v.findViewById(R.id.btnNoOfArrows);
      final Button btnLow = (Button)v.findViewById(R.id.btnLow);
      final Button btnMid = (Button)v.findViewById(R.id.btnMed);
      final Button btnHigh = (Button)v.findViewById(R.id.btnHigh);

      btnArrow.setText("-" + arrowBtnDesc[arr] + "-");
      btnHigh.setText("-" + scoringMatrix[arr][0] + "-");
      btnMid.setText("-" + scoringMatrix[arr][1] + "-");
      btnLow.setText("-" + scoringMatrix[arr][2] + "-");
    }
  }
}
