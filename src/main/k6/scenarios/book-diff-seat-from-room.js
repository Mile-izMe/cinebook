import http from "k6/http";
import {check, sleep} from "k6";

const BASE_URL = 'http://localhost:8080';

export const options = {vus: 1, iterations: 1};

const payload = JSON.stringify({
    showtimeId: "44d5e208-83fa-4176-97ba-421c9da50057",
    seatIds: [
        "75f418fb-3241-4bfc-bbd0-0c339cfdb7c5"
    ],
    guestEmail: "race@test.com",
    guestPhone: "0987654321"
})

const params = {
    headers: {
        "Content-Type": "application/json",
    },
};

export default function () {

    const res = http.post(
        (`${BASE_URL}/api/bookings`),
        payload,
        params
    );

    console.log("Status:", res.status);
    console.log("Body:", res.body);
    
    check(res, {
        "status is fail": (r) =>
            r.status === 400,
    });

}