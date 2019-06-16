// Set new default font family and font color to mimic Bootstrap's default styling
Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';

// Bar Chart Example
var myBarChart;

function createConditionChart(labels, data){
	var ctx = document.getElementById("myBarChart");
	myBarChart = new Chart(ctx, {
	  type: 'bar',
	  data: {
	    labels: labels,
	    datasets: [{
	      label: "Condition",
	      backgroundColor: "#4e73df",
	      hoverBackgroundColor: "#2e59d9",
	      borderColor: "#4e73df",
	      data: data,
	    }],
	  },
	  options: {
	    maintainAspectRatio: false,
	    layout: {
	      padding: {
	        left: 10,
	        right: 25,
	        top: 25,
	        bottom: 0
	      }
	    },
	    scales: {
	      xAxes: [{
	        time: {
	          unit: 'player'
	        },
	        gridLines: {
	          display: false,
	          drawBorder: false
	        },
	        ticks: {
	          maxTicksLimit: 6
	        },
	        maxBarThickness: 25,
	      }],
	      yAxes: [{
	        ticks: {
	          min: 0,
	          max: 100,
	          maxTicksLimit: 5,
	          padding: 10,
	          
	        },
	        gridLines: {
	          color: "rgb(234, 236, 244)",
	          zeroLineColor: "rgb(234, 236, 244)",
	          drawBorder: false,
	          borderDash: [2],
	          zeroLineBorderDash: [2]
	        }
	      }],
	    },
	    legend: {
	      display: false
	    },
	    tooltips: {
	      titleMarginBottom: 10,
	      titleFontColor: '#6e707e',
	      titleFontSize: 14,
	      backgroundColor: "rgb(255,255,255)",
	      bodyFontColor: "#858796",
	      borderColor: '#dddfeb',
	      borderWidth: 1,
	      xPadding: 15,
	      yPadding: 15,
	      displayColors: false,
	      caretPadding: 10,
	      
	    },
	  }
	});
}

$.get("/player/condition", function(data) {
	var allPlayerNames = data.map(function (player) {
		  return player.surname;  
		});
	var allPlayerConditions = data.map(function (player) {
		  return player.condition;  
		});
	createConditionChart(allPlayerNames, allPlayerConditions);
}, "json");

//setInterval(function() {
//	$.get("/player/condition", function(data) {
//		
//		var allPlayerConditions = data.map(function (player) {
//			  return player.condition;  
//			});
//		myBarChart.data.datasets[0].data=allPlayerConditions;
//		myBarChart.update()
//	}, "json");
//}, 10000);
