/**
 * Created by h4llow3En on 31/10/14.
 */
$(document).ready(function() {
    'use strict';

    $('#form').submit(function(e) {

        if(!$('#use_ajax').is(':checked')) {
            return;
        }
        e.preventDefault();

        var form = $(this);

        $.ajax({
            type	: 'POST',
            cache	: false,
            url		: form.attr('action'),
            data	: form.serialize(),
            success	: function(data) {
                $("#entries").append(data);
                $('html, body').animate({scrollTop: form.offset().top}, 2000);
                e.target.reset();
            }
        });
    });

    $('#entries').on('submit','form', function(e){

        if(!$('#use_ajax').is(':checked')) {
            return;
        }
        e.preventDefault();
        var form = $(this);

        var id = form.attr('data-entry-id');

        $.ajax({
            type	: 'DELETE',
            cache	: false,
            url		: form.attr('action'),
            data	: form.serialize(),
            success	: function() {
                $('#entry' + id).slideUp(500, function() {
                    $(this).remove();
                });
            }
        });
    });
});