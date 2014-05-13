$(function() {
    var sourcesWorking = $("#sources-working");
    var sourceLIs = sourcesWorking.find(".source");
    sourceLIs.each(function() {
        var sourceLI = $(this);
        var slug = sourceLI.data("source-slug");
        $.ajax(jsRoutes.controllers.BuildingController.subInfo(placeRef, slug))
            .done(function(data) {
                sourceLI.appendTo($("#sources-done"));
                sourceLI.addClass(data['result']);
                sourceLI.find(".sub-details").html(data['html']);
                if (sourcesWorking.find(".source").length === 0) {
                    sourcesWorking.hide();
                }
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
                console.log('subinfo failure!');
            })
    });
});