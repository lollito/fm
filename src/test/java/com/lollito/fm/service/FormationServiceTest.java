package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;

@SpringBootTest
@Transactional
public class FormationServiceTest {

    @Autowired
    private FormationService formationService;

    @Test
    public void testFormationOrdering() {
        Formation formation = new Formation();
        Module module = new Module("4-4-2", 2, 2, 4, 0, 2);
        formation.setModule(module);

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Player p = new Player();
            p.setId((long) i);
            p.setSurname("Player" + i);
            // Give them a natural role that is NOT matching their position (except maybe some)
            p.setRole(PlayerRole.FORWARD);
            players.add(p);
        }
        formation.setPlayers(players);

        // First player is GK (index 0)
        assertNotNull(formation.getGoalKeeper());
        assertEquals("Player0", formation.getGoalKeeper().getSurname());

        // Next 2 are CD (Indices 1, 2)
        List<Player> cds = formation.getCentralDefenders();
        assertEquals(2, cds.size());
        assertEquals("Player1", cds.get(0).getSurname());
        assertEquals("Player2", cds.get(1).getSurname());

        // Next 2 are WB (Indices 3, 4)
        List<Player> wbs = formation.getWingBacks();
        assertEquals(2, wbs.size());
        assertEquals("Player3", wbs.get(0).getSurname());
        assertEquals("Player4", wbs.get(1).getSurname());

        // Next 4 are MF (Indices 5, 6, 7, 8)
        List<Player> mfs = formation.getMidfielders();
        assertEquals(4, mfs.size());
        assertEquals("Player5", mfs.get(0).getSurname());
        assertEquals("Player8", mfs.get(3).getSurname());

        // Next 0 are WNG
        List<Player> wngs = formation.getWings();
        assertEquals(0, wngs.size());

        // Next 2 are FW (Indices 9, 10)
        List<Player> fws = formation.getForwards();
        assertEquals(2, fws.size());
        assertEquals("Player9", fws.get(0).getSurname());
        assertEquals("Player10", fws.get(1).getSurname());
    }

    @Test
    public void testValidate() {
        Formation formation = new Formation();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) players.add(new Player());
        formation.setPlayers(players);

        // Should not throw exception
        formationService.validate(formation);

        // Should throw exception if not 11 players
        formation.getPlayers().remove(0);
        assertThrows(RuntimeException.class, () -> formationService.validate(formation));
    }
}
