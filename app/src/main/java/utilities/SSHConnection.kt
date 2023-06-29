import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.BufferedReader
import java.io.InputStreamReader

class SSHConnection(private val ipAddress: String, private val password: String) {
    private var session: Session? = null

    val isConnected: Boolean
        get() = session != null && session!!.isConnected
    fun connect(): Boolean {
        try {
            val jsch = JSch()
            session = jsch.getSession("lg1", ipAddress, 22)
            session?.setPassword(password)
            session?.setConfig("StrictHostKeyChecking", "no")
            session?.connect()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
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
