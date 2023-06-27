package utilities;

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier


public class SSHConnection {

    fun SSHConnect(host: String, username: String, password: String, command: String): String {
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())

        try {
            ssh.connect(host)
            ssh.authPassword(username, password)

            val session = ssh.startSession()
            val commandResult = session.exec(command).inputStream.bufferedReader().use { it.readText() }


            return commandResult
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error"
        }
    }
}
