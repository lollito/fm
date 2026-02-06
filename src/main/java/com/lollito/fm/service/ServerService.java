package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.model.AdminRole;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.ServerResponse;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.ServerRepository;

@Service
public class ServerService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ServerRepository serverRepository;
	@Autowired private MatchRepository matchRepository;
	@Autowired private SeasonRepository seasonRepository;
	@Autowired private SeasonService seasonService;
	@Autowired private ClubService clubService;
	@Autowired private LeagueService leagueService;
	@Autowired private SimulationMatchService simulationMatchService;
	@Autowired private CountryService countryService;
	@Autowired private PlayerService playerService;
	@Autowired private UserService userService;
	@Autowired private MatchMapper matchMapper;
	
	public Server create(String serverName){
		Server server = new Server();
		server.setName(serverName);
		server.setOwner(userService.getLoggedUser());
		LocalDateTime gameStartDate = LocalDateTime.now();
		for(Country country : countryService.findByCreateLeague(true)){
			League league = new League();
			league.setName(serverName + "_" + country.getName());
			league.setCountry(country);
            league.setServer(server); // Explicitly set server
			server.addLeague(league);
			List<Club> clubs = clubService.createClubs(server, league, 10);
			league.setClubs(clubs);
			league.setCurrentSeason(seasonService.create(league, gameStartDate));
		};
		
		server.setCurrentDate(gameStartDate);
		server = serverRepository.save(server);
		return server;
	}
	
	public ServerResponse next() {
		leagueService.findAll().forEach(league -> next(league));
		return  new ServerResponse();
	}
	public ServerResponse next(League league){
		ServerResponse serverResponse = new ServerResponse();
		List<Match> matches = matchRepository.findByRoundSeasonAndDateBeforeAndFinish(league.getCurrentSeason(), LocalDateTime.now(), Boolean.FALSE);
		if(matches.isEmpty()){
			List<Player> allPlayers = new ArrayList<>();
			for(Club club : league.getClubs()) {
				for(Player player : club.getTeam().getPlayers()) {
					double increment = -((10 * player.getStamina())/99) + (1000/99);
					player.incrementCondition(increment);
					allPlayers.add(player);
				}
			}
			if(!allPlayers.isEmpty()) {
				playerService.updateSkills(allPlayers);
				playerService.saveAll(allPlayers);
			}
		} else {
			for (Match match : matches) {
				if (match.getStatus() == MatchStatus.SCHEDULED) {
					simulationMatchService.simulate(match);
				}
			}
			Match match =  matches.get(matches.size() -1);
			if(match.getLast()) {
				league.getCurrentSeason().setNextRoundNumber(match.getRound().getNumber() + 1);
				seasonRepository.save(league.getCurrentSeason());
				if(match.getRound().getLast()){
					league.addSeasonHistory(league.getCurrentSeason());
					league.setCurrentSeason(seasonService.create(league, LocalDateTime.now().plusMinutes(10)));
					leagueService.save(league);
				}
			} 
		}
		serverResponse.setDisputatedMatch(matches.stream()
				.map(matchMapper::toDto)
				.collect(Collectors.toList()));
		
		return serverResponse;
	}
	
	public ServerResponse load() {
		User user = userService.getLoggedUser();
		if (user != null && user.getServer() != null) {
			ServerResponse serverResponse = new ServerResponse();
			serverResponse.setCurrentDate(user.getServer().getCurrentDate());
			return serverResponse;
		}
		throw new EntityNotFoundException("User not assigned to any server");
	}
	
	public ServerResponse load(Long serverId) {
		Server server = serverRepository.findById(serverId)
				.orElseThrow(() -> new EntityNotFoundException("Server not found"));

		ServerResponse serverResponse = new ServerResponse();
		serverResponse.setCurrentDate(server.getCurrentDate());
		return serverResponse;
	}
	
	public List<Server> findAll(){
		return serverRepository.findAll();
	}
	
	public void delete(Long serverId){
		User user = userService.getLoggedUser();
		Server server = serverRepository.findById(serverId).orElseThrow(() -> new RuntimeException("Server not found"));

		boolean isOwner = user.equals(server.getOwner());
		boolean isAdmin = user.getAdminRole() == AdminRole.SUPER_ADMIN;

		if (!isOwner && !isAdmin) {
			throw new AccessDeniedException("Access denied");
		}
		serverRepository.deleteById(serverId);
	}

	public void deleteAll(){
		serverRepository.deleteAll();
	}
}
