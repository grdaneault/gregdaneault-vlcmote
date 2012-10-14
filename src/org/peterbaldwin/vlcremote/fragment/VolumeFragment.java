
package org.peterbaldwin.vlcremote.fragment;

import org.peterbaldwin.client.android.vlcremote.R;
import org.peterbaldwin.vlcremote.intent.Intents;
import org.peterbaldwin.vlcremote.model.Status;
import org.peterbaldwin.vlcremote.net.MediaServer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class VolumeFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, View.OnLongClickListener {

    private static final int MAX_VOLUME = 512;
    
    private TextView mVolumeNumber;
    private ImageView mIcon;
    private SeekBar mSeekBar;
    private MediaServer mMediaServer;
    
    private boolean showText;

    private BroadcastReceiver mStatusReceiver;

    public void setMediaServer(MediaServer mediaServer) {
        mMediaServer = mediaServer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.volume_fragment, root, false);
        mIcon = (ImageView) view.findViewById(android.R.id.icon);
        mIcon.setOnClickListener(this);
        mIcon.setOnLongClickListener(this);
        
        mVolumeNumber = (TextView) view.findViewById(R.id.volume_number_indicator);
        mVolumeNumber.setOnLongClickListener(this);
        mVolumeNumber.setOnClickListener(this);
        
        mSeekBar = (SeekBar) view.findViewById(android.R.id.progress);
        mSeekBar.setOnSeekBarChangeListener(this);
        
        return view;
    }

    
    /** {@inheritDoc} */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            setVolume(progress);
            setVolumeText(progress);
            
        }
    }

    /** {@inheritDoc} */
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mVolumeNumber.getVisibility() == View.GONE)
        {
            mVolumeNumber.setVisibility(View.VISIBLE);
            mVolumeNumber.setWidth(mIcon.getWidth());
            mIcon.setVisibility(View.GONE);
            setVolumeText(seekBar.getProgress());
        }

    }

    /** {@inheritDoc} */
    public void onStopTrackingTouch(SeekBar seekBar) {
        setVolume(seekBar.getProgress());
        setVolumeText(seekBar.getProgress());
        
        if (showText)
        {
            mIcon.setVisibility(View.GONE);
            mVolumeNumber.setVisibility(View.VISIBLE);
        }
        else
        {
            mIcon.setVisibility(View.VISIBLE);
            mVolumeNumber.setVisibility(View.GONE);
        }
    }

    private void setVolume(int value) {
        mMediaServer.status().command.volume(value);
        setVolumeText(value);
    }
    
    private void setVolumeText(int progress)
    {
        String prog = "" + (progress * 100 / (MAX_VOLUME / 2));
        mVolumeNumber.setText(prog);
    }

    void onVolumeChanged(int value) {
        mIcon.setImageResource(getVolumeImage(value));
        mSeekBar.setProgress(value);
    }

    private static int getVolumeImage(int volume) {
        if (volume == 0) {
            return R.drawable.ic_media_volume_muted;
        } else if (volume < (MAX_VOLUME / 3)) {
            return R.drawable.ic_media_volume_low;
        } else if (volume < (2 * MAX_VOLUME / 3)) {
            return R.drawable.ic_media_volume_medium;
        } else {
            return R.drawable.ic_media_volume_high;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mStatusReceiver = new StatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_STATUS);
        getActivity().registerReceiver(mStatusReceiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mStatusReceiver);
        mStatusReceiver = null;
        super.onPause();
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intents.ACTION_STATUS.equals(action)) {
                Status status = (Status) intent.getSerializableExtra(Intents.EXTRA_STATUS);
                onVolumeChanged(status.getVolume());
            }
        }
    }

    public void onClick(View v) {
        if (mSeekBar.getProgress() == 0)
        {
            setVolume(256);
            onVolumeChanged(256);
        }
        else if (mSeekBar.getProgress() == 256)
        {
            setVolume(512);
            onVolumeChanged(512);
        }
        else if (mSeekBar.getProgress() == 512)
        {
            setVolume(0);
            onVolumeChanged(0);
        }
        else
        {
            if (mSeekBar.getProgress() <= 128)
            {
                setVolume(0);
                onVolumeChanged(0);
            }
            else if (mSeekBar.getProgress() <= 384)
            {
                setVolume(256);
                onVolumeChanged(256);
            }
            else
            {
                setVolume(512);
                onVolumeChanged(512);
            }
        }
    }

    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        if (v == mIcon || v== mVolumeNumber)
        {
            showText = !showText;
            
            if (showText)
            {
                mIcon.setVisibility(View.GONE);
                mVolumeNumber.setVisibility(View.VISIBLE);
            }
            else
            {
                mIcon.setVisibility(View.VISIBLE);
                mVolumeNumber.setVisibility(View.GONE);
            }
            return true;
        }
        
        return false;
    }
}
