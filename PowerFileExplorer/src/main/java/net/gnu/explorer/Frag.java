package net.gnu.explorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.CopyFileCheck;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.files.EncryptDecryptUtils;
import com.amaze.filemanager.utils.files.Futils;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.demo.MediaPlayerFragment;
import com.tekinarslan.sample.PdfFragment;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.texteditor.TextFrag;
import android.view.MotionEvent;
//import org.geometerplus.android.fbreader.FBReader;

public abstract class Frag extends Fragment implements View.OnTouchListener, Cloneable, Serializable {

	private static final String TAG = "Frag";

	public TYPE type = TYPE.EXPLORER;
	public String currentPathTitle;
	protected String title;

	protected ViewGroup status;
	public ExplorerActivity activity;
	protected FragmentActivity fragActivity;
	public SharedPreferences sharedPref;
	
	ArrayList<LayoutElement> dataSourceL1 = new ArrayList<>();
	ArrayList selectedInList1 = new ArrayList();
	
	public int accentColor, primaryColor, primaryTwoColor;

	ViewGroup left;
	ViewGroup right;

	public ViewGroup commands;
	public View horizontalDivider6;

	public SlidingTabsFragment slidingTabsFragment;
	
	protected boolean fake = false;
	private Toast toast = null;
    
	public static final enum TYPE {
		EMPTY, EXPLORER, SELECTION, TEXT, WEB, PDF, PHOTO, MEDIA, APP, TRAFFIC_STATS, PROCESS//FBReader, 
		};

	public static Frag getFrag(final SlidingTabsFragment sliding, final TYPE t, final String path) {
		Frag frag = null;
		if (t == TYPE.SELECTION) {
			frag = new ContentFragment();
			frag.type = TYPE.SELECTION;
			frag.title = "Selection";
			if (path != null && path.length() > 0) {
				final List<String> asList = Arrays.asList(path.split("\\|"));
				final ContentFragment cfrag = (ContentFragment)frag;
				for (String le : asList) {
					cfrag.dataSourceL1.add(new LayoutElement(new File(le)));
				}
			}
		} else if (t == TYPE.APP) {
			frag = new AppsFragment();
		} else if (t == TYPE.PROCESS) {
			frag = new ProcessFragment();
		} else if (t == TYPE.TEXT) {
			frag = TextFrag.newInstance(null, "Text", path);
		} else if (t == TYPE.PDF) {
			frag = new PdfFragment();
		} else if (t == TYPE.WEB) {
			frag = WebFragment.newInstance(path);
//		} else if (t == TYPE.FBReader) {
//			return new FBReader();
		} else if (t == TYPE.MEDIA) {
			frag = new MediaPlayerFragment();
		} else if (t == TYPE.PHOTO) {
			frag = new PhotoFragment();
		} else if (t == TYPE.TRAFFIC_STATS) {
			frag = new DataTrackerFrag();
		} else if (t == TYPE.EXPLORER) {
			frag = new ContentFragment();
		} 
		if (frag != null) {
			frag.currentPathTitle = path;
			frag.slidingTabsFragment = sliding;
		}
		return frag;
	}

	public void updateList() {} //TODO
	public void load(String path) {}
	public void open(final int curPos, final List<LayoutElement> path) {}
	public void updateColor(View rootView) {}

	public void clone(final Frag frag, final boolean fake) {
		this.title = frag.title;
		this.currentPathTitle = frag.currentPathTitle;
		this.slidingTabsFragment = frag.slidingTabsFragment;
		this.fake = fake;
	}

	public Frag clone(final boolean fake) {
		final Frag frag = Frag.getFrag(slidingTabsFragment, type, currentPathTitle);
		frag.clone(this, fake);
		return frag;
	}

	public String getTitle() {
		if (currentPathTitle != null && currentPathTitle.length() > 0) {
			return currentPathTitle.substring(currentPathTitle.lastIndexOf("/") + 1);
		} else {
			return title;
		}
	}

	@Override
	public void onSaveInstanceState(android.os.Bundle outState) {
		//Log.d(TAG, "onSaveInstanceState" + path + ", " + outState);
		super.onSaveInstanceState(outState);
		outState.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);
		outState.putString("title", title);
	}

	@Override
	public boolean onTouch(View p1, MotionEvent p2) {
		//Log.d(TAG, "onTouch " + p1 + ", " + p2);
		if (activity != null) {
			select(true);
		}
		return false;
	}

	public boolean select(boolean sel) {
		ViewGroup statusOther;
		if (sel) {
			if (status != null) {
				status.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				if (activity.slideFrag2 != null && (statusOther = activity.slideFrag2.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				}
				activity.slideFrag1Selected = sel;
			} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
				if ((statusOther = activity.slideFrag.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				}
				activity.slideFrag1Selected = !sel;
			}
		} else {
			if (status != null) {
				status.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				if ((statusOther = activity.slideFrag2.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				}
				activity.slideFrag1Selected = sel;
			} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
				if ((statusOther = activity.slideFrag.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				}
				activity.slideFrag1Selected = !sel;
			}
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
		sharedPref = PreferenceManager.getDefaultSharedPreferences(fragActivity);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(view, savedInstanceState);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}

		setRetainInstance(true);
		AndroidUtils.setOnTouchListener(view, this);
		final Bundle args = getArguments();
		if (currentPathTitle == null && args != null) {
			title = args.getString("title");
			currentPathTitle = args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		}
		if (savedInstanceState != null) {
			title = savedInstanceState.getString("title");
			currentPathTitle = savedInstanceState.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		}
        status = (ViewGroup)view.findViewById(R.id.status);
		horizontalDivider6 = view.findViewById(R.id.horizontalDivider6);
		commands = (ViewGroup) view.findViewById(R.id.commands);

		if (type == Frag.TYPE.EXPLORER && slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			right = activity.right;
			left = activity.left;
			slidingTabsFragment.width = activity.leftSize;

		} else {
			if (activity != null) {
				right = activity.left;
				left = activity.right;
				slidingTabsFragment.width = -activity.leftSize;
			}
		}
		
		if (commands != null) {
			Button b;
			final int no = commands.getChildCount();
			for (int i = 0; i < no; i++) {
				b = (Button) commands.getChildAt(i);
				b.setTextColor(ExplorerActivity.TEXT_COLOR);
				b.getCompoundDrawables()[1].setAlpha(0xff);
				b.getCompoundDrawables()[1].setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
			}
		}
	}

	@Override
	public void onLowMemory() {
		Log.d(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
		super.onLowMemory();
		Glide.get(getContext()).clearMemory();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
		super.onStart();
	}

	@Override
    public void onResume() {
        //Log.d(TAG, "onResume " + title);
		super.onResume();
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
        //startFileObserver();
        //fixIcons(false);
    }

    protected void showToast(String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(fragActivity, message, Toast.LENGTH_SHORT);
        } else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(fragActivity, message, Toast.LENGTH_SHORT);
        } else {
			this.toast.cancel();
            // Updating toast message is showing
            this.toast.setText(message);
        }
        // Showing toast finally
        this.toast.show();
    }

//	public void onClick(final View v) {
//		Log.d(TAG, "onClick v " + v + ", " + selectedInList1.size());
//		switch (v.getId()) {
////			case R.id.history:
////                //if (ma != null)
////                    GeneralDialogCreation.showHistoryDialog(dataUtils, activity.getFutils(), this, activity.getAppTheme());
////                break;
////            case R.id.sethome:
////                if (openMode != OpenMode.FILE && openMode != OpenMode.ROOT) {
////                    Toast.makeText(activity, R.string.not_allowed, Toast.LENGTH_SHORT).show();
////                    break;
////                }
////                final MaterialDialog dialog = GeneralDialogCreation.showBasicDialog(activity,
////																					new String[]{getResources().getString(R.string.questionset),
////																						getResources().getString(R.string.setashome), getResources().getString(R.string.yes), getResources().getString(R.string.no), null});
////                dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
////						@Override
////						public void onClick(View v) {
////							home = main.getCurrentPath();
////							updatePaths(main.no);
////							dialog.dismiss();
////						}
////					});
////                dialog.show();
////                break;
////            case R.id.hiddenitems:
////                GeneralDialogCreation.showHiddenDialog(activity.dataUtils, activity.getFutils(), (ContentFragment)this, activity.getAppTheme());
////                break;
////            case R.id.search:
////                getAppbar().getSearchView().revealSearchView();
////                break;
//			case R.id.copys:
//				activity.MOVE_PATH = null;
//				ArrayList<BaseFile> copies = new ArrayList<>();
//				for (LayoutElement le : selectedInList1) {//int i2 = 0; i2 < plist.size(); i2++
//					copies.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i2))
//				}
//				activity.COPY_PATH = copies;
//
//				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.curExplorerFrag.commands.getVisibility() == View.GONE) {//type == -1
//					activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//					activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
//					activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
//					activity.curExplorerFrag.updateDelPaste();
//				} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.curContentFrag.commands.getVisibility() == View.GONE) {//type != -1
//					activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//					activity.curContentFrag.commands.setVisibility(View.VISIBLE);
//					activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
//					activity.curContentFrag.updateDelPaste();
//				}
//				break;
//			case R.id.cuts:
//				activity.COPY_PATH = null;
//				ArrayList<BaseFile> copie = new ArrayList<>();
//				for (LayoutElement le : selectedInList1) {//int i3 = 0; i3 < plist.size(); i3++
//					copie.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i3))
//				}
//				activity.MOVE_PATH = copie;
//
//				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
//					activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//					activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
//					activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
//					activity.curExplorerFrag.updateDelPaste();
//				} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.curContentFrag.commands.getVisibility() == View.GONE) {
//					activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//					activity.curContentFrag.commands.setVisibility(View.VISIBLE);
//					activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
//					activity.curContentFrag.updateDelPaste();
//				}
//				break;
//			case R.id.deletes_pastes:
//				Log.d(TAG, "deletesPastes selectedInList1.size() " + selectedInList1.size());
//				if (selectedInList1.size() > 0) {
//					GeneralDialogCreation.deleteFilesDialog(activity, //getLayoutElements(),
//															activity, selectedInList1, activity.getAppTheme());
//
//				} else {
//					String path = currentPathTitle;
//					ArrayList<BaseFile> arrayList = activity.COPY_PATH != null ? activity.COPY_PATH: activity.MOVE_PATH;
//					boolean move = activity.MOVE_PATH != null;
//					new CopyFileCheck(this, path, move, activity, ThemedActivity.rootMode)
//						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
//					//COPY_PATH = null;
//					activity.MOVE_PATH = null;
//				}
//				break;
////			case R.id.extract:
////                activity.mainActivityHelper.extractFile(selectedInList1);
////                break;
//            case R.id.renames:
//				final BaseFile f = ((LayoutElement)selectedInList1.get(0)).generateBaseFile();
//				rename(f);
//				break;
////			case R.id.ex:
////				activity.mainActivityHelper.extractFile(new File(((LayoutElement)selectedInList1.get(0)).path));
////				break;
//			case R.id.compresss:
////				ArrayList<BaseFile> copies1 = new ArrayList<>(selectedInList1.size());
////				for (LayoutElement le : selectedInList1) {
////					copies1.add(le.generateBaseFile());
////				}
////				GeneralDialogCreation.showCompressDialog(activity, copies1, currentPathTitle);
//				StringBuilder sb = new StringBuilder();
//				for (LayoutElement le : selectedInList1) {
//					sb.append(le.path).append("| ");
//				}
//				activity.compress(sb.toString(), currentPathTitle);
//				break;
//			case R.id.shares:
//				if (selectedInList1.size() > 0) {
//
//					if (selectedInList1.size() > 100)
//						Toast.makeText(activity, getResources().getString(R.string.share_limit),
//									   Toast.LENGTH_SHORT).show();
//					else {
//						ArrayList<File> arrayList = new ArrayList<>(selectedInList1.size());
//						//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//						for (LayoutElement i : selectedInList1) {//plist
//							arrayList.add(new File(i.path));//dataSourceL1.get(
//						}
//
//						switch (dataSourceL1.get(0).getMode()) {
//							case DROPBOX:
//							case BOX:
//							case GDRIVE:
//							case ONEDRIVE:
//								activity.getFutils().shareCloudFile(((LayoutElement)selectedInList1.get(0)).path,
//																	dataSourceL1.get(0).getMode(), getContext());
//								break;
//							default:
//								activity.getFutils().shareFiles(arrayList, activity, activity.getAppTheme(), accentColor);
//								break;
//						}
//					}
//				}
//				break;
//			case R.id.moreLeft:
//			case R.id.moreRight:
//				final MenuBuilder menuBuilder = new MenuBuilder(fragActivity);
//				final MenuInflater inflater = new MenuInflater(fragActivity);
//				inflater.inflate(R.menu.more_commands, menuBuilder);
//				final MenuPopupHelper optionsMenu = new MenuPopupHelper(fragActivity , menuBuilder, v);
//				optionsMenu.setForceShowIcon(true);
//
//				final int num= menuBuilder.size();
//				Drawable icon;
//				for (int i = 0; i < num; i++) {
//					icon = menuBuilder.getItem(i).getIcon();
//					if (icon != null) {
//						icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//					}
//				}
//
//				menuBuilder.setCallback(new MenuBuilder.Callback() {
//
//						@Override
//						public void onMenuModeChange(MenuBuilder p1) {
//						}
//
//						@Override
//						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
//							Log.d(TAG, item.getTitle() + ".");
//							switch (item.getItemId()) {
//								case (R.id.shortcut):
//									for (LayoutElement le : selectedInList1) {
//										AndroidUtils.addShortcut(activity, le.bf.f);
//									}
//									break;
//								case R.id.book:
//									DataUtils dataUtils = DataUtils.getInstance();
//									for (LayoutElement le : selectedInList1) {
//										dataUtils.addBook(new String[]{le.name, le.path}, true);
//									}
//									activity.refreshDrawer();
//									Toast.makeText(activity, activity.getResources().getString(R.string.bookmarksadded), Toast.LENGTH_LONG).show();
//									break;
//								case R.id.encrypts:
//									break;
//								case (R.id.hiddenfiles):
//									for (LayoutElement le : selectedInList1) {
//										activity.dataUtils.addHiddenFile(le.path);
//										if (new File(le.path).isDirectory()) {
//											File f1 = new File(le.path + "/.nomedia");
//											if (!f1.exists()) {
//												try {
//													com.amaze.filemanager.filesystem.FileUtil.mkfile(f1, activity);
//													//activity.mainActivityHelper.mkFile(new HFile(OpenMode.FILE, le.path), Frag.this);
//												} catch (Exception e) {
//													e.printStackTrace();
//												}
//											}
//											Futils.scanFile(le.path, getActivity());
//										}
//									}
//									updateList();
//									break;
//							}
//							return true;
//						}
//					});
//				optionsMenu.show();
//				break;
//			case R.id.infos:
//				LayoutElement le = (LayoutElement) selectedInList1.get(0);
//				GeneralDialogCreation.showPropertiesDialogWithPermissions(le.generateBaseFile(),
//																		  le.permissions, activity, ThemedActivity.rootMode,
//																		  activity.getAppTheme());
//				break;
//
//		}
//	};

}
