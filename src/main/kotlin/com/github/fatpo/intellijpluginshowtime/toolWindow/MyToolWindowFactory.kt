package com.github.fatpo.intellijpluginshowtime.toolWindow

import MyBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.swing.*

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.content, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow {

        val content: JPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                insets = JBUI.insets(5)
            }

            // 第一部分：展示时间戳
            val label10 = JLabel(MyBundle.message("timestamp.seconds.label"))
            val textField10 = JTextField(15).apply { isEditable = false }

            val label13 = JLabel(MyBundle.message("timestamp.milliseconds.label"))
            val textField13 = JTextField(15).apply { isEditable = false }

            val label16 = JLabel(MyBundle.message("timestamp.microseconds.label"))
            val textField16 = JTextField(15).apply { isEditable = false }

            val refreshButton = JButton(MyBundle.message("refresh.button"))

            gbc.gridx = 0
            gbc.gridy = 0
            add(label10, gbc)

            gbc.gridx = 1
            add(textField10, gbc)

            gbc.gridx = 0
            gbc.gridy = 1
            add(label13, gbc)

            gbc.gridx = 1
            add(textField13, gbc)

            gbc.gridx = 0
            gbc.gridy = 2
            add(label16, gbc)

            gbc.gridx = 1
            add(textField16, gbc)

            gbc.gridx = 1
            gbc.gridy = 3
            add(refreshButton, gbc)

            gbc.gridx = 0
            gbc.gridy = 4
            gbc.gridwidth = 2
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(JSeparator(), gbc)

            // 第二部分：10位时间戳转北京时间
            val timeStampLabel1 = JLabel(MyBundle.message("timestamp.seconds.label"))
            val timeStampField1 = JTextField(15)
            val timeStringLabel1 = JLabel(MyBundle.message("beijing.time.label"))
            val timeStringField1 = JTextField(15)
            val convertToStringButton = JButton(MyBundle.message("timestamp.to.string.button"))

            gbc.gridwidth = 1
            gbc.fill = GridBagConstraints.NONE

            gbc.gridx = 0
            gbc.gridy = 5
            add(timeStampLabel1, gbc)

            gbc.gridx = 1
            add(timeStampField1, gbc)

            gbc.gridx = 0
            gbc.gridy = 6
            add(timeStringLabel1, gbc)

            gbc.gridx = 1
            add(timeStringField1, gbc)

            gbc.gridx = 1
            gbc.gridy = 7
            add(convertToStringButton, gbc)

            gbc.gridx = 0
            gbc.gridy = 8
            gbc.gridwidth = 2
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(JSeparator(), gbc)

            // 第三部分：北京时间转10位时间戳
            val timeStringLabel2 = JLabel(MyBundle.message("beijing.time.label"))
            val timeStringField2 = JTextField(15)
            val timeStampLabel2 = JLabel(MyBundle.message("timestamp.seconds.label"))
            val timeStampField2 = JTextField(15)
            val convertToTimeStampButton = JButton(MyBundle.message("string.to.timestamp.button"))

            gbc.gridwidth = 1
            gbc.fill = GridBagConstraints.NONE

            gbc.gridx = 0
            gbc.gridy = 9
            add(timeStringLabel2, gbc)

            gbc.gridx = 1
            add(timeStringField2, gbc)

            gbc.gridx = 0
            gbc.gridy = 10
            add(timeStampLabel2, gbc)

            gbc.gridx = 1
            add(timeStampField2, gbc)

            gbc.gridx = 1
            gbc.gridy = 11
            add(convertToTimeStampButton, gbc)

            // 添加事件监听器
            refreshButton.addActionListener {
                refreshTimeStamps(
                    textField10,
                    textField13,
                    textField16,
                    timeStampField1,
                    timeStringField1,
                    timeStringField2,
                    timeStampField2
                )
            }

            convertToStringButton.addActionListener {
                convertTimeStampToString(timeStampField1, timeStringField1)
            }

            convertToTimeStampButton.addActionListener {
                convertStringToTimeStamp(timeStringField2, timeStampField2)
            }

            // 初始化界面
            refreshTimeStamps(
                textField10,
                textField13,
                textField16,
                timeStampField1,
                timeStringField1,
                timeStringField2,
                timeStampField2
            )
        }

        private fun refreshTimeStamps(
            textField10: JTextField,
            textField13: JTextField,
            textField16: JTextField,
            timeStampField1: JTextField,
            timeStringField1: JTextField,
            timeStringField2: JTextField,
            timeStampField2: JTextField
        ) {
            val currentTimeMillis = System.currentTimeMillis()
            val timeStamp13 = currentTimeMillis.toString()

            val currentTimeSeconds = currentTimeMillis / 1000
            val timeStamp10 = currentTimeSeconds.toString()

            val currentTimeMicros = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now())
            val timeStamp16 = currentTimeMicros.toString()

            textField10.text = timeStamp10
            textField13.text = timeStamp13
            textField16.text = timeStamp16

            timeStampField1.text = timeStamp10
            val timeStr = getTimeStr(timeStamp10)
            timeStringField1.text = timeStr.first
            timeStringField2.text = timeStr.first
            timeStampField2.text = timeStamp10
        }

        private fun getTimeStr(origin10BitTsStr: String): Pair<String, String> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            val timeStamp = origin10BitTsStr.toLong()
            val instant = Instant.ofEpochSecond(timeStamp)
            val beijingTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"))
            val beijingTimeStr = beijingTime.format(formatter)
            val utcTimeStr = DateTimeFormatter.ISO_INSTANT.format(instant)

            return Pair(beijingTimeStr, utcTimeStr)
        }

        private fun convertTimeStampToString(
            timeStampField: JTextField, timeStringField: JTextField
        ) {
            try {
                val timeStamp = timeStampField.text.trim().toLong()
                val instant = Instant.ofEpochSecond(timeStamp)
                val beijingTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"))
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                timeStringField.text = beijingTime.format(formatter)
            } catch (e: NumberFormatException) {
                JOptionPane.showMessageDialog(content, "invalid timestamp", "error", JOptionPane.ERROR_MESSAGE)
            }
        }

        private fun convertStringToTimeStamp(
            timeStringField: JTextField, timeStampField: JTextField
        ) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val beijingTime = LocalDateTime.parse(timeStringField.text.trim(), formatter)
                val instant = beijingTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant()
                timeStampField.text = instant.epochSecond.toString()
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    content,
                    "invalid string formatter, should be: 2021-01-01 12:00:01",
                    "error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }
}