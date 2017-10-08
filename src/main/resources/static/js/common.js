function buildTemplateFromGameResponse(data){
	var source = $("#entry-template").html();
	var template = Handlebars.compile(source);
	currentDate = data.currentDate.dayOfMonth + "/" + data.currentDate.monthValue + "/" + data.currentDate.year
	var context = {
		currentDate : currentDate
	};
	var html = template(context);
	$("#entry-template-placeholder").html(html);
	if(data.currentMatch != null){
		document.location.href = "/formation";
	} else if(data.disputatedMatch.length > 0){
		var source = $("#disputated-match-template").html();
		var template = Handlebars.compile(source);
		var context = {
			disputatedMatch : data.disputatedMatch
		};
		var html = template(context);
		$("#disputated-match-template-placeholder").html(html);
		$("#modal-disputated-match").modal("show");
	}
}

$("#menu-toggle").click(function(e) {
	e.preventDefault();
	$("#wrapper").toggleClass("toggled");
});


$("#next").click(function(e) {
	e.preventDefault();
	var data = {}
	if($('#formation-form').length){
		data = $('#formation-form').serialize();
	}
	$.ajax({
		method : "POST",
		url : "/game/next",
		data: data
	}).done(function(data) {
		buildTemplateFromGameResponse(data);
	});
});

$(document).ready(function() {
	$.get("/game/", function(data) {
		buildTemplateFromGameResponse(data);
	}, "json");

});