
package semtex.archery;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ExternalStorageManager;
import semtex.archery.entities.data.entities.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.commonsware.cwac.tlv.TouchListView;
import com.commonsware.cwac.tlv.TouchListView.DropListener;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.RuntimeExceptionDao;


public class Scoring extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = Scoring.class.getName();

  private Visit currentVisit;

  private Target currentTarget;

  private static final int MAX_ARROWS = 4;

  protected static final int RC_SCOREBOARD = 2;

  private static final int RC_TAKE_PICTORE = 3;

  private static final int MENU_START_STOP_ORDERING = 1;

  private static final int MENU_CREATE_NEW_VERSION = 2;

  private final Map<UserVisit, Integer> userPoints = new HashMap<UserVisit, Integer>();

  private final Map<UserVisit, TargetHit> userTargetHits = new HashMap<UserVisit, TargetHit>();

  private ViewPagerAdapter viewPageAdapter;

  private ArrayAdapter<UserVisit> adapter;

  private final int scoringMatrix[][] = new int[5][];

  private final String arrowBtnDesc[] = new String[5];

  private RuntimeExceptionDao<UserVisit, Long> userVisitDao;

  private boolean editMode = false;

  private final DropListener dropListener = new DropListener() {

    public void drop(final int from, final int to) {
      if (from == to) {
        Log.d(TAG, "no move registered!");
        return;
      } // if - nothing to do

      Log.i(TAG, "moving from " + from + " to " + to);
      final UserVisit targetVisit = adapter.getItem(from);
      // if we insert after the original position
      if (from < to) {
        // set rank to new one
        targetVisit.setRank(to);
        // move all other items after the source to the position of the source
        for (int i = from + 1; i <= to; i++) {
          final UserVisit moveItem = adapter.getItem(i);
          moveItem.setRank(moveItem.getRank() - 1);
          userVisitDao.update(moveItem);
        } // for

        // otherwise make space for the new element in terms of moving everything backward till you find the "space" at
        // the new old source.
      } else if (from > to) {
        targetVisit.setRank(to);

        // for
        for (int i = to; i < from; i++) {
          final UserVisit moveItem = adapter.getItem(i);
          moveItem.setRank(moveItem.getRank() + 1);
          userVisitDao.update(moveItem);
        } // for
      } // else
      userVisitDao.update(targetVisit);
      updateUIElements();
    } // drop
  }; // DropListener


  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, MENU_START_STOP_ORDERING, Menu.NONE, R.string.start_stop_edit_mode);
    menu.add(0, MENU_CREATE_NEW_VERSION, Menu.NONE, R.string.menu_create_new_version);
    return true;
  };


  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId()) {
      case MENU_START_STOP_ORDERING:
        final TouchListView listView = (TouchListView)findViewById(R.id.tlvUserScoring);
        editMode = !editMode;
        if (editMode) {
          listView.setDropListener(dropListener);
        } else {
          listView.setDropListener(null);
        }
        adapter.notifyDataSetChanged();
        return true;
      case MENU_CREATE_NEW_VERSION:
        askNewParcourVersion();
        return true;
    }
    return false;
  }


  private void askNewParcourVersion() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Really create new version?");
    builder.setMessage("This will create a new version of the current parcour!");
    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

      public void onClick(final DialogInterface dialog, final int which) {
        generateNewParcourVersion();
      }
    });
    builder.setNegativeButton("No", null);
    builder.show();
  }


  protected void generateNewParcourVersion() {
    final Version oldVersion = currentVisit.getVersion();
    getHelper().getVersionDao().refresh(oldVersion);

    final Version newVersion = new Version();
    newVersion.setParcour(currentVisit.getVersion().getParcour());
    newVersion.setCreated(new Date());

    getHelper().getVersionDao().create(newVersion);

    // copy all the older targets
    for (final Target target : getHelper().getTargetDao().findTargetsByVersion(oldVersion)) {
      // copy all entries till the one before our current position
      if (target.getTargetNumber() <= currentTarget.getTargetNumber()) {
        final Target newTarget = new Target(target.getTargetNumber(), newVersion);

        // don't copy images, gps locations, ... for the "current" image, as it's the first one that changed
        if (target.getTargetNumber() != currentTarget.getTargetNumber()) {
          newTarget.setComment(target.getComment());
          newTarget.setLatitude(target.getLatitude());
          newTarget.setLongitude(target.getLongitude());
          newTarget.setPictureLocation(target.getPictureLocation());
        } // if

        getHelper().getTargetDao().create(newTarget);

        // re-map the targetHits
        for (final TargetHit oldTH : getHelper().getTargetHitDao().findTargetHitsByVisitAndTarget(currentVisit, target)) {
          oldTH.setTarget(newTarget);
          getHelper().getTargetHitDao().update(oldTH);
        } // for
      } else {
        // we have to remove all old target hits from this target that won't be accessible anyway after the re-mapping
        final List<TargetHit> oldTargetHits =
            getHelper().getTargetHitDao().findTargetHitsByVisitAndTarget(currentVisit, target);
        getHelper().getTargetHitDao().delete(oldTargetHits);
      } // else
    } // for

    currentVisit.setVersion(newVersion);
    getHelper().getVisitDao().update(currentVisit);
    // reload everything from database!
    fetchSetupData();
    updateUIElements();
    Toast.makeText(getApplicationContext(), "Successfully changed to new version", Toast.LENGTH_LONG).show();
  } // generateNewParcourVersion


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    setContentView(R.layout.scoring_main);

    userVisitDao = getHelper().getUserVisitDao();

    initScoringMatrix();

    final ImageButton btnNext = (ImageButton)findViewById(R.id.btnNext);
    btnNext.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        saveResultsAndSwap(true);
      }
    });

    final ImageButton btnLast = (ImageButton)findViewById(R.id.btnPrev);
    btnLast.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        saveResultsAndSwap(false);
      }
    });

    viewPageAdapter = new ViewPagerAdapter();
    final ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
    viewPager.setAdapter(viewPageAdapter);

    fetchSetupData();
    updateUIElements();
  }


  @Override
  protected void onPause() {
    saveResults();
    super.onPause();
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

    viewPageAdapter.updateUI();

  }


  protected void saveResults() {
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
  }


  protected void saveResultsAndSwap(final boolean forward) {
    saveResults();

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
    currentTarget =
        getHelper().getTargetDao().findTargetByTargetNumber(
            getHelper().getTargetHitDao().getLatestTargetNumber(currentVisit), currentVisit.getVersion());
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


  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    // if we get the "close down" message from scoreboard => follow its lead!
    if (requestCode == RC_SCOREBOARD && resultCode == RESULT_OK) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
      finish();

    } else if (requestCode == RC_TAKE_PICTORE && resultCode == RESULT_OK) {
      final String location = currentTarget.getId().toString() + ".jpg";
      currentTarget.setPictureLocation(location);
      getHelper().getTargetDao().update(currentTarget);
      updateUIElements();
    }
  }

  public class ViewPagerAdapter extends PagerAdapter {

    private View scoringView = null;

    private View photoView = null;


    public void updateUI() {
      if (scoringView != null) {
        final TouchListView listView = (TouchListView)scoringView.findViewById(R.id.tlvUserScoring);
        adapter =
            new UserVisitAdapter(Scoring.this.getApplicationContext(), R.layout.scoring_user_row, new LinkedList(
                userTargetHits.keySet()));
        adapter.sort(new RankComparator());
        listView.setAdapter(adapter);
      }
      if (photoView != null) {
        final ImageViewTouch ivt = (ImageViewTouch)photoView.findViewById(R.id.targetGestureImage);
        ivt.clear();

        final AsyncTask<String, Void, Bitmap> loader = new AsyncTask<String, Void, Bitmap>() {

          @Override
          protected Bitmap doInBackground(final String... params) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

              final String location = params[0];

              final File targetDir = ExternalStorageManager.getApplicationPath();
              if (targetDir == null || location == null) {
                return BitmapFactory.decodeResource(getResources(), R.drawable.not_available);
              }

              final File targetFile = new File(targetDir, location);
              if (targetFile.exists()) {
                final Uri targetUri = Uri.fromFile(targetFile);
                return readBitmap(targetUri);
              }
            }
            return BitmapFactory.decodeResource(getResources(), R.drawable.not_available);
          } // doInBackground


          @Override
          protected void onPostExecute(final Bitmap result) {
            ivt.setImageBitmap(result, true);
          } // onPostExecute

        };
        loader.execute(currentTarget.getPictureLocation());
      } // if photoview != null
    } // updateUi


    public Bitmap readBitmap(final Uri selectedImage) {
      Bitmap bm = null;
      final BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 2; // reduce quality
      AssetFileDescriptor fileDescriptor = null;
      try {
        fileDescriptor = Scoring.this.getContentResolver().openAssetFileDescriptor(selectedImage, "r");
      } catch(final FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        try {
          bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
          fileDescriptor.close();
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }
      return bm;

    }


    @Override
    public int getCount() {
      return 2;
    }


    @Override
    public boolean isViewFromObject(final View view, final Object object) {
      return view.equals(object);
    }


    @Override
    public Object instantiateItem(final View pager, final int position) {
      switch (position) {
        case 0:
          if (scoringView == null) {
            final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            scoringView = li.inflate(R.layout.scoring_scoring, null);
            final Button btnScoring = (Button)scoringView.findViewById(R.id.btnScoring);
            btnScoring.setOnClickListener(new View.OnClickListener() {

              public void onClick(final View v) {
                final Intent i = new Intent(getApplicationContext(), Scoreboard.class);
                i.putExtra("visit_id", currentVisit.getId());
                startActivityForResult(i, RC_SCOREBOARD);
              }
            });
          }
          ((ViewPager)pager).addView(scoringView, 0);
          updateUI();
          return scoringView;
        case 1:
          if (photoView == null) {
            final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            photoView = li.inflate(R.layout.scoring_targetinfo, null);

            final Button takePicture = (Button)photoView.findViewById(R.id.btnTakePicture);
            takePicture.setOnClickListener(new View.OnClickListener() {

              public void onClick(final View v) {
                if (ExternalStorageManager.isExternalStorageAvail()) {
                  final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                  final File target =
                      new File(ExternalStorageManager.getApplicationPath(), currentTarget.getId().toString() + ".jpg");

                  final Uri outputFileUri = Uri.fromFile(target);
                  intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                  startActivityForResult(intent, RC_TAKE_PICTORE);

                }
              }
            });

          }
          ((ViewPager)pager).addView(photoView, 0);
          updateUI();
          return photoView;
        default:
          return null;
      }
    }
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
          Log.d(TAG, "Changed used arrows to " + usedArrows + " for " + th.getUser());
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
      final String arrowButtonDescription = arrowBtnDesc[th.getNrOfArrows()];

      Log.d(TAG, "Updating arrow button to: " + arrowButtonDescription);
      btnArrow.setText("-" + arrowButtonDescription + "-");
      btnArrow.setVisibility(editMode ? View.INVISIBLE : View.VISIBLE);
      final TextView txtPoints = (TextView)v.findViewById(R.id.txtPoints);
      txtPoints.setText(points + " pts");

      for (final ToggleButton btn : buttonContainer.keySet()) {
        final int buttonValue = scoringMatrix[th.getNrOfArrows()][buttonContainer.get(btn)];
        Log.d(TAG, "ButtonValue: " + buttonValue);
        final String buttonText = "-" + buttonValue + "-";
        btn.setText(buttonText);
        btn.setTextOn(buttonText);
        btn.setTextOff(buttonText);
        btn.setVisibility(editMode ? View.INVISIBLE : View.VISIBLE);
      }

      if (editMode) {
        return;
      }

      int i = 0;
      if (th.getPoints() != null) {
        Log.d(TAG, "Points set - searching for button");
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
        Log.d(TAG, "Points not set (" + th.getPoints() + ") or button not found");
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
        visStateBtn.setChecked(false);
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
