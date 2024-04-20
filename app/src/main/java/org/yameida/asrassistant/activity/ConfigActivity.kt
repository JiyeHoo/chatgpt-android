package org.yameida.asrassistant.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.CompoundButton
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.android.synthetic.main.activity_config.rl_api_key
import kotlinx.android.synthetic.main.activity_config.rl_assistant_name
import kotlinx.android.synthetic.main.activity_config.rl_bar
import kotlinx.android.synthetic.main.activity_config.rl_gpt_model
import kotlinx.android.synthetic.main.activity_config.rl_log
import kotlinx.android.synthetic.main.activity_config.rl_share
import kotlinx.android.synthetic.main.activity_config.sw_use_context
import org.yameida.asrassistant.R
import org.yameida.asrassistant.config.Config
import org.yameida.asrassistant.utils.HttpUtil
import org.yameida.asrassistant.utils.ShareUtil
import java.io.File


class ConfigActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        initView()
    }

    private fun initView() {
        rl_api_key.setOnClickListener { showCorpIdDialog() }
        rl_assistant_name.setOnClickListener { showRenameDialog() }
        rl_gpt_model.setOnClickListener { showModelDialog() }
        rl_log.setOnClickListener { showLog() }
        rl_share.setOnClickListener { showShareDialog() }
        sw_use_context.isChecked = Config.useContext
        sw_use_context.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_use_context onCheckedChanged: $isChecked")
            Config.useContext = isChecked
        })

        rl_bar.setNavigationOnClickListener { finish() }
    }

    private fun showCorpIdDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("API_KEY")
            .setDefaultText(Config.apiKey)
            .setPlaceholder("sk-xxxxxx")
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.confirm)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    Config.apiKey = text.toString().trim()
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showRenameDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("昵称")
            .setDefaultText(Config.assistantName)
            .setPlaceholder("请输入昵称")
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.confirm)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    Config.assistantName = text.toString().trim()
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showModelDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("GPT模型")
            .setDefaultText(Config.gptModel)
            .setPlaceholder("请输入GPT模型")
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.confirm)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    Config.gptModel = text.toString().trim()
                    HttpUtil.gptRequestJson["model"] = Config.gptModel
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

//    private fun showDonateDialog() {
//        DonateUtil.zfbDonate(this)
//    }

    private fun showLog() {
        val file = File(filesDir, "log.txt")

        val contentUri = FileProvider.getUriForFile(this, "org.yameida.asrassistant.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        startActivity(Intent.createChooser(shareIntent, "Share File"))

    }

    private fun showShareDialog() {
        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = ShareUtil.TEXT
            putExtra(Intent.EXTRA_TEXT, "这是子妍的毕设，地址: https://github.com/jiyehoo/chatgpt-android")
        }, "分享"))
    }

}

