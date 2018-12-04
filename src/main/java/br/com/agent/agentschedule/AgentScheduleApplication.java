package br.com.agent.agentschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgentScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentScheduleApplication.class, args);
	}
}
