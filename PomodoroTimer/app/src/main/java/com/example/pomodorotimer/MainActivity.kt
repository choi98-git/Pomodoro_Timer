package com.example.pomodorotimer

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }

    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }

    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }

    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null


    private val soundPool = SoundPool.Builder().build()

    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    private fun bindViews(){
        seekBar.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        updateRemainTimes(progress * 60 * 1000L)
                    }
                }

                // 타이머 실행중 새로운 터치를 감지하면 실행
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopCountDown()
                }

                // 사용자가 seekBar 위치를 조정 후 터치를 멈추면 그 기준의 seekBar 값으로 타이머 시작
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return
                    if (seekBar.progress == 0){
                        stopCountDown()
                    } else {
                        startCountDown()
                    }
                }

            }
        )
    }


    private  fun initSounds(){
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId= soundPool.load(this,R.raw.timer_bell, 1)
    }

    private fun createCountDownTimer(initialMillis: Long) =
        object :CountDownTimer(initialMillis, 1000L){
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTimes(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }

        }

    private fun startCountDown(){
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L )
        currentCountDownTimer?.start()

        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun stopCountDown(){
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPool.autoPause()
    }

    private fun completeCountDown(){
        updateRemainTimes(0)
        updateSeekBar(0)

        soundPool.autoPause()

        bellSoundId?.let {soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    // 1초마다 시간을 나타내는 TextView 상태 업데이트
    @SuppressLint("SetTextI18n")
    private fun updateRemainTimes(remainMillis: Long) {
        val remainSeconds = remainMillis / 1000

        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60)
    }

    // 1초마다 seekBar 상태 업데이트
    private fun updateSeekBar(remainMillis: Long) {
        seekBar.progress = (remainMillis / 1000 / 60).toInt()
    }

}