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

            gbc.gridwidth = 1
            gbc.fill = GridBagConstraints.NONE

            val timeStampLabel = JLabel(MyBundle.message("timestamp.seconds.label"))
            val timeStampField = JTextField(15)
            val beijingTimeLabel = JLabel(MyBundle.message("beijing.time.label"))
            val beijingTimeField = JTextField(15)
            val utcTimeLabel = JLabel(MyBundle.message("utc.time.label"))
            val utcTimeField = JTextField(15)
            val convertToStringButton = JButton(MyBundle.message("timestamp.to.string.button"))
            val convertToTimeStampButton = JButton(MyBundle.message("string.to.timestamp.button"))

            gbc.gridx = 0
            gbc.gridy = 5
            add(timeStampLabel, gbc)

            gbc.gridx = 1
            add(timeStampField, gbc)

            gbc.gridx = 0
            gbc.gridy = 6
            add(beijingTimeLabel, gbc)

            gbc.gridx = 1
            add(beijingTimeField, gbc)

            gbc.gridx = 0
            gbc.gridy = 7
            add(utcTimeLabel, gbc)

            gbc.gridx = 1
            add(utcTimeField, gbc)

            gbc.gridx = 0
            gbc.gridy = 8
            add(convertToStringButton, gbc)

            gbc.gridx = 1
            add(convertToTimeStampButton, gbc)

            refreshButton.addActionListener {
                refreshTimeStamps(textField10, textField13, textField16, timeStampField, beijingTimeField, utcTimeField)
            }

            convertToStringButton.addActionListener {
                convertTimeStampToString(timeStampField, beijingTimeField, utcTimeField)
            }

            convertToTimeStampButton.addActionListener {
                convertStringToTimeStamp(beijingTimeField, timeStampField, utcTimeField)
            }

            refreshTimeStamps(textField10, textField13, textField16, timeStampField, beijingTimeField, utcTimeField)
        }

        private fun refreshTimeStamps(
            textField10: JTextField,
            textField13: JTextField,
            textField16: JTextField,
            timeStampField: JTextField,
            beijingTimeField: JTextField,
            utcTimeField: JTextField
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

            val timeStr = getTimeStr(timeStamp10)
            timeStampField.text = timeStamp10
            beijingTimeField.text = timeStr.first
            utcTimeField.text = timeStr.second
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
            timeStampField: JTextField, beijingTimeField: JTextField, utcTimeField: JTextField
        ) {
            try {
                val timeStamp = timeStampField.text.trim().toLong()
                val instant = Instant.ofEpochSecond(timeStamp)
                val beijingTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"))
                val utcTime = DateTimeFormatter.ISO_INSTANT.format(instant)

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                beijingTimeField.text = beijingTime.format(formatter)
                utcTimeField.text = utcTime
            } catch (e: NumberFormatException) {
                JOptionPane.showMessageDialog(content, "invalid timestamp", "ERROR", JOptionPane.ERROR_MESSAGE)
            }
        }

        private fun convertStringToTimeStamp(
            beijingTimeField: JTextField, timeStampField: JTextField, utcTimeField: JTextField
        ) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val beijingTime = LocalDateTime.parse(beijingTimeField.text.trim(), formatter)
                val instant = beijingTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant()
                timeStampField.text = instant.epochSecond.toString()

                val utcTime = beijingTime.atZone(ZoneId.of("Asia/Shanghai")).withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime()
                utcTimeField.text = utcTime.format(formatter)
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    content, "invalid time str, should be: 2024-07-18 12:05:05", "ERROR", JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }
}
