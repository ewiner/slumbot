var exampleChanged = false;
var exampleDestination = "CpQBhwAAAFgqcR6l_AkSi1-BRW4ks5XEjyGySFd3HGPxSTNPlKrMwm5-pVOA4wakQcqmdPJCoH6GuiKaLIGeZRs455HuBPfpt8Sxed713Clqss6cdI-sCHChh14FniQXGZRGm4ZAyCVMnsuPVsh7tQMBSlqjr7jr63YzfXoMeq3iEmtcjWyei9g_lhjgBVOamziPYs33bxIQAF9h3yaRH46ZNHU2w_54zhoUArC4vFBjtowu4PUesR2M7ti7f1I";

var VALID_COUNTIES = ['New York County', 'Kings County', 'Queens County', 'Richmond County', 'Bronx County'];
var NYC_CENTER = new google.maps.LatLng(40.7692907, -73.8931637);

function showValidationError(msg) {
    $("#search-box").addClass("has-error");
    $("#search-box .help-block").text(msg);
    $("#search-box .help-block").show();
    $("#pac-input").on('input propertychange paste', function() {
        $("#search-box").removeClass("has-error");
        $("#search-box .help-block").hide();
    });
    console.log("validation error: " + msg);
}

function arrayStartsWith(arr, expected) {
    return arr.length >= 1 && arr[0] === expected;
}

function findAddressComponent(place, componentType) {
    var addresses = place.address_components;

    for (var i = 0; i < addresses.length; i++) {
        var address = addresses[i];
        if (arrayStartsWith(address.types, componentType)) {
            return address;
        }
    }
    return false;
}

function validatePlace(place) {

    if (!arrayStartsWith(place.types, 'street_address')) {
        showValidationError("Please choose a valid building address.");
        return false;
    }

    var county = findAddressComponent(place, 'administrative_area_level_2');
    if (!county || $.inArray(county.long_name, VALID_COUNTIES) === -1) {
        showValidationError("Please choose an address in New York City."); // TODO: suggest one!
        return false;
    }

    return true;
}

function loadPlace(placeRef) {
    // TODO: feedback / spinner while it's loading
    document.location = jsRoutes.controllers.BuildingController.infoPage(placeRef).url;
}

function initAddressSearch() {
    $("#search-btn").off('click.example');

    var searchBox = $("#pac-input");
    searchBox.val("");

    var autocomplete = new google.maps.places.Autocomplete(searchBox[0], {
        bounds: new google.maps.LatLngBounds(NYC_CENTER, NYC_CENTER),
        types: ['geocode']  // addresses only, no buildings or landmarks
    });

    google.maps.event.addListener(autocomplete, 'place_changed', function() {
        var place = autocomplete.getPlace();
        if (!place.geometry) {
            return;
        }

        if (!validatePlace(place)) {
            return;
        }

        loadPlace(place.reference);
    });
}

$(function() {

    $("#search-btn").on('click.example', function() {
        loadPlace(exampleDestination);
    });
    $("#pac-input").one('focus', initAddressSearch);

});
