let allPlayers = [];

function selectRoleFormatter(value, row, index) {
    const roles = [
        { value: 0, name: 'GK', fullName: 'GOALKEEPER' },
        { value: 1, name: 'DEF', fullName: 'DEFENDER' },
        { value: 2, name: 'WB', fullName: 'WINGBACK' },
        { value: 3, name: 'MF', fullName: 'MIDFIELDER' },
        { value: 4, name: 'WNG', fullName: 'WING' },
        { value: 5, name: 'FW', fullName: 'FORWARD' }
    ];
    let options = roles.map(role => {
        const isSelected = (row.role === role.fullName) || (typeof row.role === 'object' && (row.role.value === role.value || row.role.name === role.fullName));
        return `<option value="${role.value}" ${isSelected ? 'selected' : ''}>${role.name}</option>`;
    }).join('');
    return `<select class="form-control role-select" data-player-id="${row.id}">${options}</select>`;
}

function operateFormatter(value, row, index) {
    return `<div id="${row.id}" data-name="${row.surname}" class="player-drag" draggable="true" ondragstart="drag(event)" style="cursor:grab;">
                <i class="fas fa-futbol"></i> ${row.surname}
            </div>`;
}

function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev) {
    ev.dataTransfer.setData("text", ev.target.id || $(ev.target).closest('.player-drag').attr('id'));
}

function drop(ev) {
    ev.preventDefault();
    var playerId = ev.dataTransfer.getData("text");
    var $pos = $(ev.target).closest('.pos');

    if ($pos.length > 0) {
        $pos.find('input[name="playersId"]').val(playerId);
        const roleValue = $pos.data('role');
        if (roleValue !== undefined) {
            // Update player role to match the slot
            $.post(`/api/player/${playerId}/change-role?role=${roleValue}`, function() {
                $('#players-table').bootstrapTable('refresh');
                updatePitchPlayer($pos, playerId);
            });
        } else {
            updatePitchPlayer($pos, playerId);
        }
    }
}

function updatePitchPlayer($pos, playerId) {
    const player = allPlayers.find(p => p.id == playerId);
    if (player) {
        $pos.find('.player-name').text(player.surname);
        $pos.addClass('occupied');
    } else {
        $.get('/api/player/', function(players) {
            allPlayers = players;
            const p = allPlayers.find(p => p.id == playerId);
            if (p) {
                $pos.find('.player-name').text(p.surname);
                $pos.addClass('occupied');
            }
        });
    }
}

function updatePitch(moduleId) {
    $.get('/api/module/', function(modules) {
        const module = modules.find(m => m.id == moduleId);
        if (module) {
            renderPitch(module);
        }
    });
}

function renderPitch(module) {
    const $container = $('.formation-container');
    $container.find('.pos:not(.gk)').remove();
    $('.pos.gk').data('role', 0); // GK role value

    let positions = [];

    // Defs (Role 1)
    for(let i=0; i<module.def; i++) positions.push({role: 1, top: '80%', left: (10 + (80/(module.def+1))*(i+1)) + '%'});
    // Wingbacks (Role 2)
    for(let i=0; i<module.wb; i++) positions.push({role: 2, top: '70%', left: (i % 2 === 0 ? '10%' : '90%')});
    // Mids (Role 3)
    for(let i=0; i<module.mf; i++) positions.push({role: 3, top: '50%', left: (10 + (80/(module.mf+1))*(i+1)) + '%'});
    // Wings (Role 4)
    for(let i=0; i<module.wng; i++) positions.push({role: 4, top: '40%', left: (i % 2 === 0 ? '5%' : '95%')});
    // Forwards (Role 5)
    for(let i=0; i<module.fw; i++) positions.push({role: 5, top: '20%', left: (10 + (80/(module.fw+1))*(i+1)) + '%'});

    positions.forEach((p, index) => {
        const $slot = $(`
            <div class="pos" ondrop="drop(event)" ondragover="allowDrop(event)" style="position: absolute; top: ${p.top}; left: ${p.left}; transform: translate(-50%, -50%);">
                <span class="player-name">${getRoleAbbreviation(p.role)}</span>
                <input type="hidden" name="playersId" />
            </div>
        `);
        $slot.data('role', p.role);
        $container.append($slot);
    });
}

function getRoleAbbreviation(roleValue) {
    switch(roleValue) {
        case 0: return 'GK';
        case 1: return 'DEF';
        case 2: return 'WB';
        case 3: return 'MF';
        case 4: return 'WNG';
        case 5: return 'FW';
        default: return 'POS';
    }
}

function getRoleName(value) {
    const roles = ['GOALKEEPER', 'DEFENDER', 'WINGBACK', 'MIDFIELDER', 'WING', 'FORWARD'];
    return roles[value];
}

function populateFormation(formation) {
    if (!formation || !formation.players) return;

    const players = formation.players;
    const roles = [0, 1, 2, 3, 4, 5];

    roles.forEach(role => {
        const roleName = getRoleName(role);
        const rolePlayers = players.filter(p => {
            const pRole = (typeof p.role === 'object' ? p.role.name : p.role);
            return pRole === roleName;
        });
        const $slots = $(`.pos`).filter(function() { return $(this).data('role') === role; });

        rolePlayers.forEach((player, i) => {
            if ($slots[i]) {
                const $slot = $($slots[i]);
                $slot.find('input[name="playersId"]').val(player.id);
                $slot.find('.player-name').text(player.surname);
                $slot.addClass('occupied');
            }
        });
    });
}

$(document).on('change', '.role-select', function() {
    const playerId = $(this).data('player-id');
    const roleValue = $(this).val();
    $.post(`/api/player/${playerId}/change-role?role=${roleValue}`, function() {
        $('#players-table').bootstrapTable('refresh');
    });
});

$('#module-template-placeholder').on('change', function() {
    updatePitch($(this).val());
});

$(document).ready(function() {
    $.get('/api/player/', function(players) {
        allPlayers = players;
    });
});
