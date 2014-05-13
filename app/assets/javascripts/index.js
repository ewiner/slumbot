

$(function() {

    var mapCenter = {lat: 40.7692907, lng: -73.8931637};

    var mapOptions = {
        center: mapCenter,
        zoom: 11,
        disableDefaultUI: true,
        mapTypeControl: true,
        rotateControl: true,
        rotateControlOptions: {
            position: google.maps.ControlPosition.RIGHT_TOP
        }
    };

//    var map = new google.maps.Map($("#map-canvas")[0], mapOptions);

    var input = $('#pac-input')[0];

//    map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

    var autocomplete = new google.maps.places.Autocomplete(input);
    autocomplete.setBounds(new google.maps.LatLngBounds(mapCenter, mapCenter));
    autocomplete.setTypes(['geocode']);

    google.maps.event.addListener(autocomplete, 'place_changed', function() {
        var place = autocomplete.getPlace();
        if (!place.geometry) {
            return;
        }

        var address = '';
        if (place.address_components) {
            address = [
                (place.address_components[0] && place.address_components[0].short_name || ''),
                (place.address_components[1] && place.address_components[1].short_name || ''),
                (place.address_components[2] && place.address_components[2].short_name || '')
            ].join(' ');
        }

        print(place.name);
        print(address);
    });
});
