package utilities

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.Properties


class SSHConnection(private val ipAddress: String, private val password: String) {
    private var session: Session? = null

    val isConnected: Boolean
        get() = session != null && session!!.isConnected
    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsch = JSch()
                val session = jsch.getSession("lg", ipAddress, 22)
                session.setPassword(password)

                val prop = Properties()
                prop.put("StrictHostKeyChecking", "no")
                session.setConfig(prop)

                session.connect()

                val channel = session.openChannel("exec") as ChannelExec
                val baos = ByteArrayOutputStream()
                channel.outputStream = baos

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun disconnect() {
        session?.disconnect()
        session = null
    }

    fun executeCommand(command: String): String {
        val output = StringBuilder()

        if (session != null && session?.isConnected == true) {
            try {
                val channel = session?.openChannel("exec") as ChannelExec
                channel.setCommand(command)

                val inputStream = channel.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                channel.connect()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }

                channel.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return output.toString()
    }
}
