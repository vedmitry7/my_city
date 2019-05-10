package app.mycity.mycity.filter_desc_post;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import app.mycity.mycity.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class FilterEditImageFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private EditImageFragmentListener listener;

    @BindView(R.id.seekbar_brightness)
    SeekBar seekBarBrightness;

    @BindView(R.id.seekbar_contrast)
    SeekBar seekBarContrast;

    @BindView(R.id.seekbar_saturation)
    SeekBar seekBarSaturation;

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    public FilterEditImageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_edit_image, container, false);

        ButterKnife.bind(this, view);

        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);

        seekBarContrast.setMax(20);
        seekBarContrast.setProgress(0);

        seekBarSaturation.setMax(30);
        seekBarSaturation.setProgress(10);

        seekBarBrightness.setOnSeekBarChangeListener(this);
        seekBarContrast.setOnSeekBarChangeListener(this);
        seekBarSaturation.setOnSeekBarChangeListener(this);

        return view;
    }


    enum Changes{Brightness,Contrast,Saturation}

    Changes changes;

    float changesValue;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (listener != null) {

            if (seekBar.getId() == R.id.seekbar_brightness) {
                changesValue = progress - 100;
                changes = Changes.Brightness;
            }

            if (seekBar.getId() == R.id.seekbar_contrast) {
                progress += 10;
                float floatVal = .10f * progress;
                changesValue = floatVal;
                changes = Changes.Contrast;

            }

            if (seekBar.getId() == R.id.seekbar_saturation) {
                float floatVal = .10f * progress;
                changesValue = floatVal;
                changes = Changes.Saturation;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null){
            switch (changes){
                case Brightness:
                    listener.onBrightnessChanged((int) changesValue);
                    break;
                case Contrast:
                    listener.onContrastChanged(changesValue);
                    break;
                case Saturation:
                    listener.onSaturationChanged(changesValue);
                    break;
            }
            listener.onEditCompleted();
        }
    }

    public void resetControls() {
        seekBarBrightness.setProgress(100);
        seekBarContrast.setProgress(0);
        seekBarSaturation.setProgress(10);
    }

    public interface EditImageFragmentListener {
        void onBrightnessChanged(int brightness);

        void onSaturationChanged(float saturation);

        void onContrastChanged(float contrast);

        void onEditStarted();

        void onEditCompleted();
    }
}