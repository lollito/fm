function ajaxForm(form, success) {
	form.ajaxForm({
		url : form.attr("action"),
		type : form.attr("method"),
		success : success,
		clearForm : true
	})
}

var $loading = $('#loading').hide();
$(document).ajaxStart(function() {
	console.log("entrooooo");
	$loading.show();
}).ajaxStop(function() {
	$loading.hide();
});