package br.com.agent.agentschedule;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import static br.com.agent.agentschedule.CreateFolder.createGoogleFolder;
import static br.com.agent.agentschedule.CreateGoogleFile.createGoogleFile;

@Component
public class AgentJob {

    private static final Logger log = LoggerFactory.getLogger(AgentJob.class);

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${directory.directoryFileConfig}")
    private String directoryFileConfig;

    @Value("${directory.directoryReturnConfig}")
    private String directoryReturnConfig;

    @Value("${logging.directory}")
    private String directoryLog;

    @Value("${logging.file}")
    private String logFile;

    @Value("${archive.batFile}")
    private String batFile;

    @Value("${archive.fileReturn}")
    private String fileReturn;

    @Value("${driveQuickStart.nameDefaultFolder}")
    private String nameDefaultFolder;

    @Value("${logging.file}")
    private String agentScheduleLog;

    @Scheduled(cron = "${agentScheduleJob.cron}")
    public void execute() throws GeneralSecurityException, IOException, MessagingException {

        log.info("*****************************************************");
        log.info("Starting => Agent Schedule => " + LocalDateTime.now());

        Commandline commandLine = new Commandline();

        File executable = new File(directoryFileConfig + "/" +batFile);

        boolean isVerifyDirectoryAndFile = executable.exists();

        commandLine.setExecutable(executable.getAbsolutePath());

        WriterStreamConsumer systemOut = new WriterStreamConsumer(new OutputStreamWriter(System.out));

        WriterStreamConsumer systemErr = new WriterStreamConsumer(new OutputStreamWriter(System.out));

        try {

            //exec batFile
            log.info("Starting => Exec batFile => " + LocalDateTime.now());
            log.info("Path =>  batFile => " + executable.getAbsolutePath());
            CommandLineUtils.executeCommandLine(commandLine, systemOut, systemErr);
            log.info("Finish => Agent Schedule => " + LocalDateTime.now());

            log.info("Upload => Google Driver");

            //createGoogleFolder
            com.google.api.services.drive.model.File folder = createGoogleFolder(null, nameDefaultFolder+"-"+LocalDateTime.now());
            log.info("Created folder name    => " + folder.getName());

            //createGoogleFile
            java.io.File uploadFileReturn = new java.io.File(directoryFileConfig+"/"+fileReturn);
            java.io.File uploadFileLog = new java.io.File(directoryLog+"/"+logFile);
            createGoogleFile(folder.getId(), "text/plain", "returnConfiguration"+"-"+LocalDateTime.now()+".txt", uploadFileReturn);
            createGoogleFile(folder.getId(), "text/plain", "agentScheduleLog"+"-"+LocalDateTime.now()+".log", uploadFileLog);
            log.info("Upload Ok              => Created Google file");

        } catch (CommandLineException e) {

            if (!isVerifyDirectoryAndFile){
                log.info("Atenção ===> agentScheduleJob => " + "Diretório e/ou arquivo de configuração não encontrado!");
            }

            log.error("Error ===> agentScheduleJob => " + e.getMessage());
        }
    }

}
