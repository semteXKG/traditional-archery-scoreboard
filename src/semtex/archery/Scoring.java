
package semtex.archery;

import java.util.*;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.commonsware.cwac.tlv.TouchListView;
import com.commonsware.cwac.tlv.TouchListView.DropListener;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.ForeignCollection;


public class Scoring extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = Scoring.class.getName();

  private Visit currentVisit;

  private Target currentTarget;

  private static final int MAX_ARROWS = 4;

  private final Map<UserVisit, Integer> userPoints = new HashMap<UserVisit, Integer>();

  private final Map<UserVisit, TargetHit> userTargetHits = new HashMap<UserVisit, TargetHit>();

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

    final TouchListView listView = (TouchListView)findViewById(R.id.tlvUserScoring);
    listView.setDropListener(new DropListener() {

      public void drop(final int from, final int to) {
        Log.i(TAG, "from " + from + " to " + to);
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

    final TouchListView listView = (TouchListView)findViewById(R.id.tlvUserScoring);
    final ArrayAdapter<UserVisit> adapter =
        new UserVisitAdapter(this, R.layout.scoring_user_row, new LinkedList(userTargetHits.keySet()));
    adapter.sort(new RankComparator());
    listView.setAdapter(adapter);
  }


  protected void saveResultsAndSwap(final boolean forward) {
    // Saving results to parcour page
    for (final TargetHit th : userTargetHits.values()) {
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


  private void initializeScoring() {
    for (final UserVisit uv : currentVisit.getUserVisit()) {
      final Integer points = getHelper().getTargetHitDao().calculatePointsByUser(uv);
      userPoints.put(uv, points);
      Log.i(TAG, points + " for user " + uv.getUser().getUserName());
    }
  }


  private void fetchSetupData() {
    currentVisit = getHelper().getVisitDao().findLastOpenVisit();
    Log.i(TAG, "Found: " + currentVisit);
    currentTarget = getHelper().getTargetDao().findLastTarget(currentVisit.getVersion());
    Log.i(TAG, "current Target set to: " + currentTarget);

    fillTargetHitMap();
    initializeScoring();
  }


  private void fillTargetHitMap() {
    userTargetHits.clear();

    final ForeignCollection<UserVisit> uv = currentVisit.getUserVisit();
    for (final UserVisit userVisit : uv) {
      final TargetHit th = getHelper().getTargetHitDao().findTargetHitByUserVisitAndTarget(userVisit, currentTarget);
      if (th != null) {
        Log.i(TAG, "TargetHit " + th + " found for " + userVisit);
        userTargetHits.put(userVisit, th);
      } else {
        Log.i(TAG, "Creating new TargetHit for " + userVisit);
        final TargetHit newHit = new TargetHit(null, 1, userVisit, currentTarget);
        userTargetHits.put(userVisit, newHit);
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

      final Map<ToggleButton, Integer> scoringButtons = new HashMap<ToggleButton, Integer>();

      final UserVisit uv = getItem(position);
      if (rowView == null) {
        final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        rowView = li.inflate(R.layout.scoring_user_row, null);
      }

      final View finalRowView = rowView;

      scoringButtons.put((ToggleButton)rowView.findViewById(R.id.btnLow), 2);
      scoringButtons.put((ToggleButton)rowView.findViewById(R.id.btnMed), 1);
      scoringButtons.put((ToggleButton)rowView.findViewById(R.id.btnHigh), 0);

      final TextView userName = (TextView)rowView.findViewById(R.id.txtUsername);
      userName.setText(uv.getUser().getUserName());

      final TextView points = (TextView)rowView.findViewById(R.id.txtPoints);
      points.setText("-");

      final Button btnArrows = (Button)rowView.findViewById(R.id.btnNoOfArrows);
      btnArrows.setText(arrowBtnDesc[userTargetHits.get(uv).getNrOfArrows()]);

      btnArrows.setOnClickListener(new View.OnClickListener() {

        public void onClick(final View v) {
          final TargetHit th = userTargetHits.get(uv);
          incrementUsedArrows(th);
          updateHitButtons(finalRowView, scoringButtons, th, userPoints.get(uv));
        }


        private int incrementUsedArrows(final TargetHit th) {

          final int usedArrows = th.getNrOfArrows() % MAX_ARROWS + 1;
          th.setNrOfArrows(usedArrows);
          return usedArrows;
        }

      });

      updateHitButtons(finalRowView, scoringButtons, userTargetHits.get(uv), userPoints.get(uv));

      for (final ToggleButton tb : scoringButtons.keySet()) {
        tb.setOnClickListener(new View.OnClickListener() {

          public void onClick(final View v) {
            final ToggleButton btn = (ToggleButton)v;
            final TargetHit th = userTargetHits.get(uv);
            if (btn.isChecked()) {
              setPoints(uv, th, scoringMatrix[th.getNrOfArrows()][scoringButtons.get(btn)]);
            } else {
              setPoints(uv, th, null);
            }
            updateHitButtons(finalRowView, scoringButtons, th, userPoints.get(uv));
          }

        });
      }

      final GradientDrawable gd =
          new GradientDrawable(Orientation.RIGHT_LEFT, new int[] {
              uv.getUser().getRgbColor() & 0x00FFFFFF | 0xAA000000, 0x0 });
      rowView.setBackgroundDrawable(gd);
      return rowView;
    }


    private void setPoints(final UserVisit visit, final TargetHit th, final Integer pointsArg) {
      if (pointsArg == null) {
        userPoints.put(visit, userPoints.get(visit) - th.getPoints());
        th.setPoints(null);
      } else {
        userPoints.put(visit, userPoints.get(visit) + pointsArg);
        th.setPoints(pointsArg);
      }
    }


    private void updateHitButtons(final View v, final Map<ToggleButton, Integer> buttonContainer, final TargetHit th,
        final int points) {
      final Button btnArrow = (Button)v.findViewById(R.id.btnNoOfArrows);
      btnArrow.setText("-" + arrowBtnDesc[th.getNrOfArrows()] + "-");
      final TextView txtPoints = (TextView)v.findViewById(R.id.txtPoints);
      txtPoints.setText(points + " pts");

      for (final ToggleButton btn : buttonContainer.keySet()) {
        final String buttonText = "-" + scoringMatrix[th.getNrOfArrows()][buttonContainer.get(btn)] + "-";
        btn.setText(buttonText);
        btn.setTextOn(buttonText);
        btn.setTextOff(buttonText);
      }

      int i = 0;
      if (th.getPoints() != null) {
        // determine the right button - search in the index columns
        while (i < scoringMatrix[th.getNrOfArrows()].length) {
          if (scoringMatrix[th.getNrOfArrows()][i] == th.getPoints()) {
            changeButtonStyles(true, findToggleButtonForIndex(buttonContainer, i), buttonContainer.keySet());
            btnArrow.setEnabled(false);
            break;
          }
          i++;
        }
      }
      if (th.getPoints() == null || i == scoringMatrix[th.getNrOfArrows()].length) {
        changeButtonStyles(false, null, buttonContainer.keySet());
        btnArrow.setEnabled(true);
      }
    }


    private ToggleButton findToggleButtonForIndex(final Map<ToggleButton, Integer> buttonContainer, final int i) {
      for (final Map.Entry<ToggleButton, Integer> entry : buttonContainer.entrySet()) {
        if (entry.getValue().intValue() == i) {
          return entry.getKey();
        }
      }
      return null;
    }


    protected void changeButtonStyles(final boolean pointsSelected, final ToggleButton toggleButton,
        final Collection<ToggleButton> allToggleButtons) {
      final Set<ToggleButton> toggleButtons = new HashSet<ToggleButton>(allToggleButtons);

      int visibility;

      if (pointsSelected) {
        visibility = View.INVISIBLE;
        toggleButton.setChecked(true);
        toggleButton.setVisibility(View.VISIBLE);
        toggleButton.setTypeface(null, Typeface.BOLD);
      } else {
        visibility = View.VISIBLE;
      }

      toggleButtons.remove(toggleButton);
      for (final ToggleButton visStateBtn : toggleButtons) {
        visStateBtn.setTypeface(null, Typeface.NORMAL);
        visStateBtn.setVisibility(visibility);
      }
    }
  }

  private class RankComparator implements Comparator<UserVisit> {

    public int compare(final UserVisit lhs, final UserVisit rhs) {
      if (lhs == null && rhs == null) {
        return 0;
      }
      if (lhs == null) {
        return 1;
      }
      if (rhs == null) {
        return -1;
      }
      return lhs.getRank().compareTo(rhs.getRank());
    }
  }
}
