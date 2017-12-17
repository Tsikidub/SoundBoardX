package io.github.xtonousou.soundboardx;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.ViewHolder>
		implements Filterable {
	private static final String TAG = "SoundAdapter";
	private static final int WRITE_SETTINGS_PERMISSION = 1337;

	private Activity activity;
	private Typeface font;
	private ArrayList<Sound> sounds;
	private ArrayList<Sound> soundsCopy;

	SoundAdapter(Activity activity) {
		this.activity = activity;
		this.font = Typeface.createFromAsset(activity.getAssets(),
				"fonts/Roboto-Regular.ttf");
	}

	void showPrevious() {
		SharedPrefs.getInstance().setFavoritesShown(false);
		sounds = SoundStore.getSelectedSounds(activity.getApplicationContext());
		soundsCopy = sounds;
		notifyDataSetChanged();
	}

	void showFavorites() {
		SharedPrefs.getInstance().setFavoritesShown(true);
		for (Sound sound : new ArrayList<>(sounds)) {
			if (!sound.getFavorite()) {
				notifyItemRemoved(sounds.indexOf(sound));
				sounds.remove(sound);
			}
		}
		soundsCopy = sounds;
	}

	void showAllSounds(Context context) {
		SharedPrefs.getInstance().setFavoritesShown(false);
		SharedPrefs.getInstance().setSelectedCategory(1);
		sounds = SoundStore.getAllSounds(context);
		soundsCopy = sounds;
		notifyDataSetChanged();
	}

	void showFunnySounds(Context context) {
		SharedPrefs.getInstance().setFavoritesShown(false);
		SharedPrefs.getInstance().setSelectedCategory(2);
		sounds = SoundStore.getFunnySounds(context);
		soundsCopy = sounds;
		notifyDataSetChanged();
	}

	void showGamesSounds(Context context) {
		SharedPrefs.getInstance().setFavoritesShown(false);
		SharedPrefs.getInstance().setSelectedCategory(3);
		sounds = SoundStore.getGamesSounds(context);
		soundsCopy = sounds;
		notifyDataSetChanged();
	}

	void showMoviesSounds(Context context) {
		SharedPrefs.getInstance().setFavoritesShown(false);
		SharedPrefs.getInstance().setSelectedCategory(4);
		sounds = SoundStore.getMoviesSounds(context);
		soundsCopy = sounds;
		notifyDataSetChanged();
	}

	void showMusicSounds(Context context) {
		SharedPrefs.getInstance().setFavoritesShown(false);
		SharedPrefs.getInstance().setSelectedCategory(5);
		sounds = SoundStore.getMusicSounds(context);
		soundsCopy = sounds;
		notifyDataSetChanged();
	}

	boolean handleWriteSettingsPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.System.canWrite(activity.getApplicationContext())) {
				return true;
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity.getApplicationContext());

				builder.setMessage(R.string.modify_settings)
						.setTitle(R.string.allow_access);

				builder.setPositiveButton(R.string.settings, (dialog, id) -> {
					Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
					intent.setData(uri);
					activity.startActivity(intent);
				});

				builder.setNegativeButton(R.string.not_now, (dialog, id) -> Toast.makeText
						(activity.getApplicationContext(), R.string.permission_needed, Toast
								.LENGTH_LONG).show());

				AlertDialog dialog = builder.create();
				dialog.show();

				if (Settings.System.canWrite(activity.getApplicationContext())) {
					return true;
				}
			}
		}
		return true;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements
			View.OnCreateContextMenuListener,
			MenuItem.OnMenuItemClickListener {
		public final TextView title;
		final ImageButton favButton;

		ViewHolder(View v) {
			super(v);
			title = v.findViewById(R.id.title);
			favButton = v.findViewById(R.id.fav_button);

			title.setTypeface(font);
			title.setTextColor(activity.getResources().getColor(R.color.colorAccent));

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				@Subscribe(threadMode = ThreadMode.ASYNC)
				public void onClick(View view) {
					if (EventBus.getDefault().isRegistered(this)) return;

					if (SharedPrefs.getInstance().areAnimationsShown())
						new ParticleManager(new Particle(itemView),
								title.getText().toString()).emit();

					EventBus.getDefault().register(this);
					EventBus.getDefault().post(sounds.get(getAdapterPosition()));
					EventBus.getDefault().unregister(this);
				}
			});

			v.setOnCreateContextMenuListener(this);
		}


		@Override
		public void onCreateContextMenu(final ContextMenu contextMenu, View view,
										ContextMenu.ContextMenuInfo contextMenuInfo) {
			contextMenu.setHeaderTitle(R.string.header);
			contextMenu.setHeaderIcon(
					new IconicsDrawable(view.getContext())
							.icon(FontAwesome.Icon.faw_music)
							.color(activity.getResources().getColor(R.color.colorAccent))
							.sizeDp(24)
			);

			MenuItem setRingtone = contextMenu.add(R.string.ringtone);
			MenuItem setNotification = contextMenu.add(R.string.notification);
			MenuItem setAlarm = contextMenu.add(R.string.alarm);

			setRingtone.setOnMenuItemClickListener(this);
			setNotification.setOnMenuItemClickListener(this);
			setAlarm.setOnMenuItemClickListener(this);
		}

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			handleWriteSettingsPermission();
			ToneManager tone = new ToneManager(activity, itemView, title.getText().toString(),
					getAdapterPosition());
			switch (menuItem.getTitle().toString()) {
				case "Set as ringtone":
					tone.setToneAs((byte) 0);
					break;
				case "Set as notification":
					tone.setToneAs((byte) 1);
					break;
				case "Set as alarm":
					tone.setToneAs((byte) 2);
					break;
			}
			return true;
		}
	}

	@Override
	public SoundAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_card, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		Context favContext = holder.favButton.getContext();
		int color = activity.getResources().getColor(R.color.colorAccent);

		Utils.paintThis(holder);
		holder.title.setText(sounds.get(position).getName());

		boolean isFavorite = sounds.get(position).getFavorite();

		holder.favButton.setImageDrawable(isFavorite
				? new IconicsDrawable(favContext).icon(FontAwesome.Icon.faw_heart)
				.color(color)
				.sizeDp(24)
				: new IconicsDrawable(favContext)
				.icon(FontAwesome.Icon.faw_heart_o)
				.color(color)
				.sizeDp(24));

		holder.favButton.setOnClickListener(v -> {
			Context vContext = v.getContext();
			try {
				boolean newFavStatus = !sounds.get(holder.getAdapterPosition()).getFavorite();
				sounds.get(holder.getAdapterPosition()).setFavorite(newFavStatus);

				if (newFavStatus) {
					((ImageButton) v).setImageDrawable(
							new IconicsDrawable(vContext)
									.icon(FontAwesome.Icon.faw_heart)
									.color(color)
									.sizeDp(24)
					);
				} else {
					((ImageButton) v).setImageDrawable(
							new IconicsDrawable(vContext)
									.icon(FontAwesome.Icon.faw_heart_o)
									.color(color)
									.sizeDp(24)
					);
				}

				if (SharedPrefs.getInstance().areFavoritesShown()) {
					// Remove from the list.
					sounds.remove(holder.getAdapterPosition());
					notifyItemRemoved(holder.getAdapterPosition());
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(TAG, e.getMessage());
			}
		});
	}

	@Override
	public int getItemCount() {
		return sounds != null ? sounds.size() : 0;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private Filter mFilter = new Filter() {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			Filter.FilterResults filterResults = new Filter.FilterResults();
			ArrayList<Sound> tempList = new ArrayList<>();
			if (constraint != null) {
				for (Sound item : soundsCopy) {
					if (Utils.striptease(item.getName().toLowerCase())
							.contains(Utils.striptease(constraint.toString().toLowerCase()))) {
						tempList.add(item);
					}
				}
				filterResults.values = tempList;
				filterResults.count = tempList.size();
			}
			return filterResults;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			sounds = (ArrayList<Sound>) results.values;
			notifyDataSetChanged();
		}
	};
}
