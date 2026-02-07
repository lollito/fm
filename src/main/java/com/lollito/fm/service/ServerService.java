package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.model.AdminRole;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.ServerResponse;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.ServerRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ServerService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ServerRepository serverRepository;
	@Autowired private MatchRepository matchRepository;
	@Autowired private SeasonRepository seasonRepository;
	@Autowired private LeagueRepository leagueRepository;
	@Autowired private ClubRepository clubRepository;
	@Autowired private PlayerRepository playerRepository;
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
	
	@Transactional
	public ServerResponse next() {
		List<League> leagues = leagueRepository.findAllWithCurrentSeason();
		if (leagues.isEmpty()) return new ServerResponse();

		List<Season> seasons = leagues.stream().map(League::getCurrentSeason).collect(Collectors.toList());
		List<Match> allMatches = matchRepository.findByRoundSeasonInAndDateBeforeAndFinish(seasons, LocalDateTime.now(), Boolean.FALSE);

		Map<Season, List<Match>> matchesBySeason = allMatches.stream()
				.collect(Collectors.groupingBy(m -> m.getRound().getSeason()));

		List<League> leaguesWithoutMatches = new ArrayList<>();

		for (League league : leagues) {
			Season season = league.getCurrentSeason();
			List<Match> matches = matchesBySeason.getOrDefault(season, Collections.emptyList());

			if (matches.isEmpty()) {
				leaguesWithoutMatches.add(league);
			} else {
				matches.sort((m1, m2) -> m1.getDate().compareTo(m2.getDate()));

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
		}

		if(!leaguesWithoutMatches.isEmpty()) {
			List<Club> clubs = clubRepository.findAllByLeagueInWithTeam(leaguesWithoutMatches);

			List<Long> teamIds = clubs.stream()
					.map(c -> c.getTeam().getId())
					.collect(Collectors.toList());

			if(!teamIds.isEmpty()) {
				List<Player> players = playerRepository.findByTeamIdIn(teamIds);
				for(Player player : players) {
					double increment = -((10 * player.getStamina())/99) + (1000/99);
					player.incrementCondition(increment);
				}
				playerService.updateSkills(players);
				playerService.saveAll(players);
			}
		}

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
			List<Match> scheduledMatches = matches.stream()
					.filter(match -> match.getStatus() == MatchStatus.SCHEDULED)
					.collect(Collectors.toList());

			if (!scheduledMatches.isEmpty()) {
				simulationMatchService.simulate(scheduledMatches);
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
