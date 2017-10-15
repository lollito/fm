function buildTemplateFromGameResponse(data, showToastr){
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
		if($('#ranking-table').length){
			$('#ranking-table').bootstrapTable('refresh');
		}
		
	} else if(showToastr) {
		toastr.options = {
				  "closeButton": true,
				  "debug": false,
				  "newestOnTop": false,
				  "progressBar": true,
				  "positionClass": "toast-top-center",
				  "preventDuplicates": false,
				  "onclick": null,
				  "showDuration": "300",
				  "hideDuration": "500",
				  "timeOut": "1000",
				  "extendedTimeOut": "500",
				  "showEasing": "swing",
				  "hideEasing": "linear",
				  "showMethod": "slideDown",
				  "hideMethod": "fadeOut"
				}
        toastr.info('No match today');
	}
}

$("#menu-toggle").click(function(e) {
	e.preventDefault();
	$("#wrapper").toggleClass("toggled");
});


$("#next").click(function(e) {
	e.preventDefault();
	$.ajax({
		method : "POST",
		url : "/game/next",
	}).done(function(data) {
		buildTemplateFromGameResponse(data, true);
	});
});

$(document).ready(function() {
	$.get("/game/", function(data) {
		buildTemplateFromGameResponse(data, false);
	}, "json");

});