package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.mapper.ServerMapper;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.dto.ServerDTO;
import com.lollito.fm.model.rest.ServerResponse;
import com.lollito.fm.service.ServerService;

@RestController
@RequestMapping(value="/api/server")
public class ServerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ServerService serverService;
	@Autowired private ServerMapper serverMapper;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
    public ServerResponse create(@RequestParam(required = true) String serverName) {
		Server server = serverService.create(serverName);
        return new ServerResponse(server.getCurrentDate());
    }
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public ServerResponse game() {
		return serverService.load();
    }
	
	@RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public List<ServerDTO> findAll() {
		return serverService.findAll().stream()
			.map(serverMapper::toDto)
			.collect(Collectors.toList());
    }
	
	@RequestMapping(value = "/next", method = RequestMethod.POST)
    public ServerResponse next() {
		return serverService.next();
    }
	
	@RequestMapping(value = "/load", method = RequestMethod.GET)
    public ServerResponse load(Long serverId) {
		return serverService.load(serverId);
    }
	
	@RequestMapping(value = "/", method = RequestMethod.DELETE)
    public String delete(Long serverId) {
		serverService.delete(serverId);
		return "ok";
    }
	
}
