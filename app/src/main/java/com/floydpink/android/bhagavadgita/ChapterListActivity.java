package com.floydpink.android.bhagavadgita;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.floydpink.android.bhagavadgita.helpers.ChapterHelper;
import com.floydpink.android.bhagavadgita.helpers.ShareHelper;
import com.floydpink.android.bhagavadgita.helpers.TypefaceSpan;

/**
 * An activity representing a list of Chapters. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ChapterDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ChapterListFragment} and the item details
 * (if present) is a {@link ChapterDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ChapterListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ChapterListActivity extends Activity
        implements ChapterListFragment.Callbacks, ChapterDetailFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Selected chapter name
     */
    private String mChapterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the malayalam title on this activity
        SpannableString s = new SpannableString(getTitle());
        s.setSpan(new TypefaceSpan(this, "AnjaliOldLipi.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setTitle(s);

        setContentView(R.layout.activity_chapter_list);

        if (findViewById(R.id.chapter_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ChapterListFragment) getFragmentManager()
                    .findFragmentById(R.id.chapter_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
        final Intent intent = getIntent();
        final String action = intent.getAction();

        // if the app is launched from a deep link, navigate to the child/grandchild activity
        if (Intent.ACTION_VIEW.equals(action)) {
//  *** UNCOMMENT BELOW TO ATTACH DEBUGGER ***
//            Log.d("Starting 10 seconds delay:", "Attach the debugger");
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                public void run() {
            final String url = intent.getDataString();
            int baseUrlLength = ShareHelper.BASE_URL.length();
            if (url.indexOf(ShareHelper.BASE_URL) == 0 && url.length() > baseUrlLength) {
                String queryString = url.substring(baseUrlLength + 1);
                String[] parts = queryString.split("[=&]");
                int chapterIndex = -1;
                int sectionIndex = -1;
                if (parts.length == 4) {    // deep link to a section
                    chapterIndex = Integer.parseInt(parts[1]);
                    sectionIndex = Integer.parseInt(parts[3]);
                } else if (parts.length == 2) { //deep link to a chapter
                    chapterIndex = Integer.parseInt(parts[1]);
                }
                String chapterName = ChapterHelper.getChapterFromChapterIndex(chapterIndex).getName();
                selectChapter(chapterName, sectionIndex != -1 ? queryString : "");
            }
//                }
//            }, 10000);
        }
    }

    /**
     * Callback method from {@link ChapterListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onChapterSelected(String chapterName) {
        selectChapter(chapterName);
    }

    private void selectChapter(String chapterName) {
        selectChapter(chapterName, "");
    }

    private void selectChapter(String chapterName, String chapterAndSectionQueryString) {
        mChapterName = chapterName;
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ChapterDetailFragment.ARG_CHAPTER_NAME, chapterName);
            if (!TextUtils.isEmpty(chapterAndSectionQueryString)) {
                arguments.putString(ChapterDetailFragment.ARG_CHAPTER_SECTION_QUERY_STRING, chapterAndSectionQueryString);
            }
            ChapterDetailFragment fragment = new ChapterDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.chapter_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ChapterDetailActivity.class);
            detailIntent.putExtra(ChapterDetailFragment.ARG_CHAPTER_NAME, chapterName);
            if (!TextUtils.isEmpty(chapterAndSectionQueryString)) {
                detailIntent.putExtra(ChapterDetailFragment.ARG_CHAPTER_SECTION_QUERY_STRING, chapterAndSectionQueryString);
            }
            startActivity(detailIntent);
        }
    }

    /**
     * Callback method from {@link ChapterDetailFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onSectionSelected(String chapterAndVerse) {
        Intent detailIntent = new Intent(this, SectionDetailActivity.class);
        detailIntent.putExtra(SectionDetailActivity.ARG_CHAPTER_VERSE, chapterAndVerse);
        startActivity(detailIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("In method:", "ChapterListActivity::onOptionsItemSelected");

        switch (id) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_share:
                ShareHelper.ShareChapter(this, mChapterName);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
