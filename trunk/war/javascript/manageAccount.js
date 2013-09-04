function showOrHideSUIdAndConfirmed() {
	if (document.getElementById("confirmedVal").innerHTML == "true") {
		document.getElementById("confirmed").style.display = "none";
	} else {
		document.getElementById("confirmed").style.display = "inline";
	}

	if (getRadioValue(document.forms["userData"]) != "SERVICE_UNIT_COORDINATOR") {
		document.getElementById("serviceUnit").style.display = "none";
	} else {
		document.getElementById("serviceUnit").style.display = "inline";
	}
}

function getRadioValue(radioButtonGroup) {
	for (i = 0; i < radioButtonGroup.length; i++) {
		if (radioButtonGroup[i].checked) {
			return radioButtonGroup[i].value;
		}
	}
	return null;
}