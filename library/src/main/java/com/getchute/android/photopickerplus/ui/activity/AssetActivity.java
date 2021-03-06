/**
 * The MIT License (MIT)

Copyright (c) 2013 Chute

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.getchute.android.photopickerplus.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.api.authentication.AuthenticationFactory;
import com.chute.sdk.v2.model.AccountModel;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ListResponseModel;
import com.chute.sdk.v2.utils.PreferenceUtil;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;
import com.getchute.android.photopickerplus.R;
import com.getchute.android.photopickerplus.models.DeliverMediaModel;
import com.getchute.android.photopickerplus.models.enums.PhotoFilterType;
import com.getchute.android.photopickerplus.ui.fragment.FragmentRoot;
import com.getchute.android.photopickerplus.ui.fragment.FragmentSingle;
import com.getchute.android.photopickerplus.ui.listener.ListenerAccountAssetsSelection;
import com.getchute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.getchute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.getchute.android.photopickerplus.ui.listener.ListenerFragmentRoot;
import com.getchute.android.photopickerplus.ui.listener.ListenerFragmentSingle;
import com.getchute.android.photopickerplus.ui.listener.ListenerImageSelection;
import com.getchute.android.photopickerplus.ui.listener.ListenerVideoSelection;
import com.getchute.android.photopickerplus.util.AssetUtil;
import com.getchute.android.photopickerplus.util.Constants;
import com.getchute.android.photopickerplus.util.FragmentUtil;
import com.getchute.android.photopickerplus.util.NotificationUtil;
import com.getchute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.getchute.android.photopickerplus.util.intent.IntentUtil;
import com.getchute.android.photopickerplus.util.intent.PhotosIntentWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying the content of the selected service.
 * 
 * This activity is used to display albums and assets for both local and remote
 * services in a GridView or ListView.
 * 
 */
public class AssetActivity extends BaseActivity implements
  ListenerFilesCursor, ListenerFilesAccount, ListenerFragmentRoot,
		ListenerFragmentSingle {

    public static final int USE_ITEM = 5;
	private static final String TAG = AssetActivity.class.getSimpleName();

	private PhotoFilterType filterType;
	private PhotosIntentWrapper wrapper;
	private FragmentRoot fragmentRoot;
	private FragmentSingle fragmentSingle;
	private AccountModel account;
	private AccountType accountType;
	private List<Integer> selectedAccountsPositions;
	private List<Integer> selectedImagesPositions;
	private List<Integer> selectedVideosPositions;
	private ListenerAccountAssetsSelection listenerAccountsSelection;
	private ListenerImageSelection listenerImagesSelection;
	private ListenerVideoSelection listenerVideosSelection;
	private String folderId;
  private Toolbar toolbar;

	public void setAssetsSelectListener(
			ListenerAccountAssetsSelection adapterListener) {
		this.listenerAccountsSelection = adapterListener;
	}

	public void setImagesSelectListener(ListenerImageSelection adapterListener) {
		this.listenerImagesSelection = adapterListener;
	}

	public void setVideosSelectListener(ListenerVideoSelection adapterListener) {
		this.listenerVideosSelection = adapterListener;
	}

  @Override
  protected int getLayoutResource() {
    return R.layout.gc_activity_assets;
  }

  @Override
  protected void setToolBar(Toolbar toolbar) {
    this.toolbar = toolbar;
  }

  public Toolbar getToolbar()  {
    return toolbar;
  }

  @SuppressLint("NewApi")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		retrieveSavedValuesFromBundle(savedInstanceState);

		wrapper = new PhotosIntentWrapper(getIntent());
		account = wrapper.getAccount();
		filterType = wrapper.getFilterType();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (savedInstanceState == null) {
			fragmentRoot = FragmentRoot.newInstance(account, filterType,
					selectedAccountsPositions, selectedImagesPositions,
					selectedVideosPositions);
			ft.add(R.id.gcFragments, fragmentRoot,
					FragmentUtil.TAG_FRAGMENT_FOLDER).commit();
		}

	}

	@Override
	public void onAccountFilesSelect(AssetModel assetModel,
			AccountModel accountModel) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this, assetModel,
      accountModel);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onCursorAssetsSelect(AssetModel assetModel) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this, assetModel,
				null);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onDeliverCursorAssets(List<DeliverMediaModel> deliverList) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this,
				AssetUtil.getPhotoCollection(deliverList), null);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onDeliverAccountFiles(ArrayList<AssetModel> assetModelList,
			AccountModel accountModel) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this,
				assetModelList, accountModel);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onAccountFolderSelect(AccountModel account, String folderId) {
		this.folderId = folderId;
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments, FragmentSingle
				.newInstance(account, folderId, selectedAccountsPositions),
				FragmentUtil.TAG_FRAGMENT_FILES);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(Constants.KEY_FOLDER_ID, folderId);
		List<Integer> accountPositions = new ArrayList<Integer>();
		List<Integer> imagePositions = new ArrayList<Integer>();
		List<Integer> videoPositions = new ArrayList<Integer>();
		if (listenerAccountsSelection != null
				&& listenerAccountsSelection.getSocialPhotosSelection() != null) {
			accountPositions.addAll(listenerAccountsSelection
					.getSocialPhotosSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS,
					(ArrayList<Integer>) accountPositions);
		}
		if (listenerImagesSelection != null
				&& listenerImagesSelection.getCursorImagesSelection() != null) {
			imagePositions.addAll(listenerImagesSelection
					.getCursorImagesSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS,
					(ArrayList<Integer>) imagePositions);
		}
		if (listenerVideosSelection != null
				&& listenerVideosSelection.getCursorVideosSelection() != null) {
			videoPositions.addAll(listenerVideosSelection
					.getCursorVideosSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS,
					(ArrayList<Integer>) videoPositions);
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		updateRootFragment();
		updateSingleFragment();

	}

	@Override
	public void onSessionExpired(AccountType accountType) {
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		AuthenticationFactory.getInstance().startAuthenticationActivity(
				AssetActivity.this, accountType);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			GCAccounts.allUserAccounts(new AccountsCallback()).executeAsync();
		}
	}

	private final class AccountsCallback implements
			HttpCallback<ListResponseModel<AccountModel>> {

		@Override
		public void onSuccess(ListResponseModel<AccountModel> responseData, ResponseStatus status) {
			if (accountType == null) {
				accountType = PhotoPickerPreferenceUtil.get().getAccountType();
			}
			if (responseData.getData().size() == 0) {
				NotificationUtil.makeToast(getApplicationContext(),
          R.string.no_albums_found);
				return;
			}
			for (AccountModel accountModel : responseData.getData()) {
				if (accountModel.getType().equals(accountType.getLoginMethod())) {
					PreferenceUtil.get().saveAccount(accountModel);
					accountClicked(accountModel.getId(), accountType.name()
							.toLowerCase(), accountModel.getShortcut());
				}
			}
		}

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			Log.d(TAG, "Http Error: " + responseStatus.getStatusCode() + " "
				+ responseStatus.getStatusMessage());
		}

	}

	public void accountClicked(String accountId, String accountName,
			String accountShortcut) {
		selectedAccountsPositions = null;
		selectedImagesPositions = null;
		selectedVideosPositions = null;
		updateRootFragment();
	}

	private void retrieveSavedValuesFromBundle(Bundle savedInstanceState) {
		selectedAccountsPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS)
				: null;

		selectedImagesPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS)
				: null;

		selectedVideosPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS)
				: null;

		folderId = savedInstanceState != null ? savedInstanceState
				.getString(Constants.KEY_FOLDER_ID) : null;
	}

	private void updateRootFragment() {
		fragmentRoot = (FragmentRoot) getSupportFragmentManager()
				.findFragmentByTag(FragmentUtil.TAG_FRAGMENT_FOLDER);
		if (fragmentRoot != null) {
			fragmentRoot.updateFragment(account, filterType,
					selectedAccountsPositions, selectedImagesPositions,
					selectedVideosPositions);
		}
	}

	private void updateSingleFragment() {
		fragmentSingle = (FragmentSingle) getSupportFragmentManager()
				.findFragmentByTag(FragmentUtil.TAG_FRAGMENT_FILES);
		if (fragmentSingle != null) {
			fragmentSingle.updateFragment(account, folderId,
					selectedAccountsPositions);
		}
	}

	public List<Integer> getPositions() {
		return selectedImagesPositions;
	}

	@Override
	public void onFragmentRootNavigationBack() {
		finish();

	}

	@Override
	public void onFragmentSingleNavigationBack() {
		FragmentUtil.replaceContentWithRootFragment(AssetActivity.this,
				account, filterType, selectedAccountsPositions,
				selectedImagesPositions, selectedVideosPositions);

	}

}