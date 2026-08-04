import http from "k6/http";
import {check, sleep} from "k6";

const BASE_URL = 'http://localhost:8080';

export const options = {vus: 5, iterations: 5};

const payload = JSON.stringify({
    showtimeId: "44d5e208-83fa-4176-97ba-421c9da50057",
    seatIds: [
        "9302bdbc-86b0-493d-8c70-cab31176905b"
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


    check(res, {
        "status is success": (r) =>
            r.status === 201,
    });

}