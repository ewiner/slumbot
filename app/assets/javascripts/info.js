$(function() {
    var sourceLIs = $("#sources-working").find(".source");
    sourceLIs.each(function() {
        var sourceLI = $(this);
        var slug = sourceLI.data("source-slug");
        $.ajax(jsRoutes.controllers.BuildingController.subInfo(placeRef, slug))
            .done(function(data) {
                sourceLI.appendTo($("#sources-done"));
                sourceLI.addClass(data['result']);
                sourceLI.find(".sub-details").html(data['html']);

            })
            .fail(function(jqXHR, textStatus, errorThrown) {
                console.log('subinfo failure!');
            })
    });
});